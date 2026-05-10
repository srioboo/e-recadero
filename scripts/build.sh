#!/bin/bash

# Build Script
# Builds both admin and front projects for production
# Output: admin/dist/ and front/dist/

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
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}E-Recadero Build Script${NC}"
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

# Build admin
echo -e "${BLUE}Building Admin Dashboard...${NC}"
cd "$ADMIN_DIR"
npm run check || { echo -e "${RED}✗ Admin TypeScript check failed${NC}"; exit 1; }
npm run build || { echo -e "${RED}✗ Admin build failed${NC}"; exit 1; }
ADMIN_SIZE=$(du -sh dist/ | cut -f1)
echo -e "${GREEN}✓ Admin build complete (Size: $ADMIN_SIZE)${NC}"
echo ""

# Build front
echo -e "${BLUE}Building Front Website...${NC}"
cd "$FRONT_DIR"
npm run check || { echo -e "${RED}✗ Front TypeScript check failed${NC}"; exit 1; }
npm run build || { echo -e "${RED}✗ Front build failed${NC}"; exit 1; }
FRONT_SIZE=$(du -sh dist/ | cut -f1)
echo -e "${GREEN}✓ Front build complete (Size: $FRONT_SIZE)${NC}"
echo ""

# Build backend
echo -e "${BLUE}Building Backend API...${NC}"
cd "$BACK_DIR"
./gradlew clean build || { echo -e "${RED}✗ Backend build failed${NC}"; exit 1; }
BACK_JAR=$(find build/libs -name "*.jar" -type f | head -1)
if [ -z "$BACK_JAR" ]; then
  echo -e "${RED}✗ Backend JAR not found${NC}"
  exit 1
fi
BACK_SIZE=$(du -sh "$BACK_JAR" | cut -f1)
echo -e "${GREEN}✓ Backend build complete (JAR: $BACK_SIZE)${NC}"
echo ""

# Summary
echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ All builds completed successfully${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo "Build Artifacts:"
echo "  Admin:   $ADMIN_DIR/dist/ (Size: $ADMIN_SIZE)"
echo "  Front:   $FRONT_DIR/dist/ (Size: $FRONT_SIZE)"
echo "  Backend: $BACK_JAR (Size: $BACK_SIZE)"
echo ""
echo "Next Steps:"
echo "  1. Local testing:  ./scripts/dev.sh"
echo "  2. Deploy:         Upload admin/dist & front/dist; Deploy backend JAR"
echo "  3. Verify:         Check https://your-domain.com and https://api.your-domain.com"
echo ""
