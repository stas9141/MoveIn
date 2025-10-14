const { PrismaClient } = require('@prisma/client');
const { logger } = require('./logger');

// Create Prisma client instance
const prisma = new PrismaClient({
  log: [
    {
      emit: 'event',
      level: 'query',
    },
    {
      emit: 'event',
      level: 'error',
    },
    {
      emit: 'event',
      level: 'info',
    },
    {
      emit: 'event',
      level: 'warn',
    },
  ],
});

// Log database queries in development
if (process.env.NODE_ENV === 'development') {
  prisma.$on('query', (e) => {
    logger.debug('Database Query', {
      query: e.query,
      params: e.params,
      duration: `${e.duration}ms`
    });
  });
}

// Log database errors
prisma.$on('error', (e) => {
  logger.error('Database Error', {
    error: e.message,
    target: e.target
  });
});

// Log database info
prisma.$on('info', (e) => {
  logger.info('Database Info', {
    message: e.message,
    target: e.target
  });
});

// Log database warnings
prisma.$on('warn', (e) => {
  logger.warn('Database Warning', {
    message: e.message,
    target: e.target
  });
});

// Connect to database
const connectDatabase = async () => {
  try {
    await prisma.$connect();
    logger.info('Database connected successfully');
    
    // Test database connection
    await prisma.$queryRaw`SELECT 1`;
    logger.info('Database connection test successful');
    
    return true;
  } catch (error) {
    logger.error('Database connection failed:', error);
    throw error;
  }
};

// Disconnect from database
const disconnectDatabase = async () => {
  try {
    await prisma.$disconnect();
    logger.info('Database disconnected successfully');
  } catch (error) {
    logger.error('Database disconnection failed:', error);
    throw error;
  }
};

// Clean up expired tokens
const cleanupExpiredTokens = async () => {
  try {
    const result = await prisma.token.deleteMany({
      where: {
        OR: [
          {
            accessTokenExpiresAt: {
              lt: new Date()
            }
          },
          {
            refreshTokenExpiresAt: {
              lt: new Date()
            }
          }
        ]
      }
    });

    if (result.count > 0) {
      logger.info(`Cleaned up ${result.count} expired tokens`);
    }

    return result.count;
  } catch (error) {
    logger.error('Token cleanup failed:', error);
    throw error;
  }
};

// Clean up expired password reset tokens
const cleanupExpiredPasswordResetTokens = async () => {
  try {
    const result = await prisma.passwordResetToken.deleteMany({
      where: {
        OR: [
          {
            expiresAt: {
              lt: new Date()
            }
          },
          {
            isUsed: true
          }
        ]
      }
    });

    if (result.count > 0) {
      logger.info(`Cleaned up ${result.count} expired password reset tokens`);
    }

    return result.count;
  } catch (error) {
    logger.error('Password reset token cleanup failed:', error);
    throw error;
  }
};

// Clean up expired email verification tokens
const cleanupExpiredEmailVerificationTokens = async () => {
  try {
    const result = await prisma.emailVerificationToken.deleteMany({
      where: {
        OR: [
          {
            expiresAt: {
              lt: new Date()
            }
          },
          {
            isUsed: true
          }
        ]
      }
    });

    if (result.count > 0) {
      logger.info(`Cleaned up ${result.count} expired email verification tokens`);
    }

    return result.count;
  } catch (error) {
    logger.error('Email verification token cleanup failed:', error);
    throw error;
  }
};

// Run all cleanup tasks
const runCleanupTasks = async () => {
  try {
    logger.info('Starting database cleanup tasks...');
    
    const tokenCount = await cleanupExpiredTokens();
    const passwordResetCount = await cleanupExpiredPasswordResetTokens();
    const emailVerificationCount = await cleanupExpiredEmailVerificationTokens();
    
    const totalCleaned = tokenCount + passwordResetCount + emailVerificationCount;
    logger.info(`Database cleanup completed. Total records cleaned: ${totalCleaned}`);
    
    return totalCleaned;
  } catch (error) {
    logger.error('Database cleanup tasks failed:', error);
    throw error;
  }
};

// Start periodic cleanup
const startPeriodicCleanup = () => {
  const interval = parseInt(process.env.TOKEN_CLEANUP_INTERVAL) || 3600000; // 1 hour default
  
  setInterval(async () => {
    try {
      await runCleanupTasks();
    } catch (error) {
      logger.error('Periodic cleanup failed:', error);
    }
  }, interval);
  
  logger.info(`Periodic cleanup started with interval: ${interval}ms`);
};

// Database health check
const healthCheck = async () => {
  try {
    await prisma.$queryRaw`SELECT 1`;
    return {
      status: 'healthy',
      timestamp: new Date().toISOString()
    };
  } catch (error) {
    logger.error('Database health check failed:', error);
    return {
      status: 'unhealthy',
      error: error.message,
      timestamp: new Date().toISOString()
    };
  }
};

module.exports = {
  prisma,
  connectDatabase,
  disconnectDatabase,
  cleanupExpiredTokens,
  cleanupExpiredPasswordResetTokens,
  cleanupExpiredEmailVerificationTokens,
  runCleanupTasks,
  startPeriodicCleanup,
  healthCheck
};

