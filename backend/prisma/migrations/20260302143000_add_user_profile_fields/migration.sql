-- AlterTable
ALTER TABLE "User"
ADD COLUMN "bio" TEXT,
ADD COLUMN "interestCategories" TEXT[] DEFAULT ARRAY[]::TEXT[];
