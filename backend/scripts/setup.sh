#!/bin/bash

# MoveIn Backend Setup Script
# This script sets up the backend development environment

set -e

echo "🚀 Setting up MoveIn Backend..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js version 18+ is required. Current version: $(node -v)"
    exit 1
fi

echo "✅ Node.js version: $(node -v)"

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL is not installed. Please install PostgreSQL 12+ first."
    exit 1
fi

echo "✅ PostgreSQL is installed"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp env.example .env
    echo "⚠️  Please edit .env file with your configuration before continuing."
    echo "   - Set DATABASE_URL"
    echo "   - Set JWT secrets"
    echo "   - Configure email settings"
    read -p "Press Enter when you've configured .env file..."
fi

# Load environment variables
source .env

# Check if database URL is set
if [ -z "$DATABASE_URL" ]; then
    echo "❌ DATABASE_URL is not set in .env file"
    exit 1
fi

# Extract database name from URL
DB_NAME=$(echo $DATABASE_URL | sed 's/.*\/\([^?]*\).*/\1/')
DB_HOST=$(echo $DATABASE_URL | sed 's/.*@\([^:]*\):.*/\1/')
DB_PORT=$(echo $DATABASE_URL | sed 's/.*:\([0-9]*\)\/.*/\1/')

echo "🗄️  Setting up database..."

# Create database if it doesn't exist
echo "Creating database: $DB_NAME"
createdb $DB_NAME 2>/dev/null || echo "Database $DB_NAME already exists"

# Run Prisma migrations
echo "🔄 Running database migrations..."
npx prisma migrate dev --name init

# Generate Prisma client
echo "🔧 Generating Prisma client..."
npx prisma generate

# Create logs directory
echo "📁 Creating logs directory..."
mkdir -p logs

# Test database connection
echo "🔍 Testing database connection..."
npx prisma db push --accept-data-loss

echo "✅ Backend setup completed successfully!"
echo ""
echo "🎉 You can now start the development server:"
echo "   npm run dev"
echo ""
echo "📚 Available commands:"
echo "   npm run dev          - Start development server"
echo "   npm start            - Start production server"
echo "   npm run db:migrate   - Run database migrations"
echo "   npm run db:studio    - Open Prisma Studio"
echo "   npm test             - Run tests"
echo ""
echo "🌐 API will be available at: http://localhost:3000"
echo "📖 API documentation: http://localhost:3000/health"

