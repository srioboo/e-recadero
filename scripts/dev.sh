#!/bin/bash

# Development Server Script
# Runs both admin and front dev servers concurrently
# Admin: http://localhost:3000
# Front: http://localhost:3001

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ADMIN_DIR="$PROJECT_ROOT/admin"
FRONT_DIR="$PROJECT_ROOT/front"
BACK_DIR="$PROJECT_ROOT/back"

# Java configuration (required for Gradle with Java 21)
export JAVA_HOME="/Users/salrio/Library/Java/JavaVirtualMachines/ms-21.0.7/Contents/Home"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}E-Recadero Development Servers${NC}"
echo "================================"
echo ""

# Check if directories exist
if [ ! -d "$ADMIN_DIR" ]; then
  echo -e "${RED}✗ Admin directory not found: $ADMIN_DIR${NC}"
  exit 1
fi

if [ ! -d "$FRONT_DIR" ]; then
  echo -e "${RED}✗ Front directory not found: $FRONT_DIR${NC}"
  exit 1
fi

if [ ! -d "$BACK_DIR" ]; then
  echo -e "${RED}✗ Backend directory not found: $BACK_DIR${NC}"
  exit 1
fi

# Check if dependencies are installed
if [ ! -d "$ADMIN_DIR/node_modules" ]; then
  echo -e "${YELLOW}Installing admin dependencies...${NC}"
  cd "$ADMIN_DIR" && npm install
fi

if [ ! -d "$FRONT_DIR/node_modules" ]; then
  echo -e "${YELLOW}Installing front dependencies...${NC}"
  cd "$FRONT_DIR" && npm install
fi

echo -e "${GREEN}✓ Starting Docker services...${NC}"
cd "$BACK_DIR" && docker compose up -d postgres redis kafka zookeeper || { echo -e "${RED}✗ Docker Compose startup failed${NC}"; exit 1; }
echo -e "${GREEN}✓ Waiting for services to be healthy...${NC}"
sleep 3

echo -e "${GREEN}✓ Starting development servers...${NC}"
echo ""
echo "Services:"
echo "  Admin Dashboard: http://localhost:3000"
echo "  Public Website:  http://localhost:3001"
echo "  Backend API:     http://localhost:8080"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"
echo ""

# Start backend in the background
cd "$BACK_DIR" && ./gradlew bootRun &
BACK_PID=$!

# Start both frontend servers in the background
cd "$ADMIN_DIR" && npm run dev &
ADMIN_PID=$!

cd "$FRONT_DIR" && npm run dev &
FRONT_PID=$!

# Trap signals to kill all processes
trap "
  echo -e '\n${YELLOW}Stopping all services...${NC}'
  kill $BACK_PID $ADMIN_PID $FRONT_PID 2>/dev/null
  cd \"$BACK_DIR\" && docker compose down 2>/dev/null
  echo -e '${YELLOW}All services stopped${NC}'
  exit 0
" SIGINT SIGTERM

# Wait for all processes
wait $BACK_PID $ADMIN_PID $FRONT_PID
