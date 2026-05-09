#!/bin/bash

# Build Script
# Builds both admin and front projects for production
# Output: admin/dist/ and front/dist/

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ADMIN_DIR="$PROJECT_ROOT/admin"
FRONT_DIR="$PROJECT_ROOT/front"

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

# Summary
echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ All builds completed successfully${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo "Build Artifacts:"
echo "  Admin: $ADMIN_DIR/dist/ (Size: $ADMIN_SIZE)"
echo "  Front: $FRONT_DIR/dist/ (Size: $FRONT_SIZE)"
echo ""
echo "Next Steps:"
echo "  1. Test locally:  npm run preview --prefix admin/"
echo "  2. Deploy:        Upload dist/ to your hosting"
echo "  3. Verify:        Check https://your-domain.com"
echo ""
