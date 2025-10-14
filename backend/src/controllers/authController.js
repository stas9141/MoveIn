const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const { PrismaClient } = require('@prisma/client');
const { logger } = require('../utils/logger');
const { sendPasswordResetEmail } = require('../services/emailService');

const prisma = new PrismaClient();

class AuthController {
  // Sign up a new user
  async signup(req, res) {
    try {
      const { email, password, first_name, last_name, phone_number } = req.body;

      // Check if user already exists
      const existingUser = await prisma.user.findUnique({
        where: { email }
      });

      if (existingUser) {
        return res.status(400).json({
          success: false,
          message: 'User with this email already exists'
        });
      }

      // Hash password
      const saltRounds = parseInt(process.env.BCRYPT_ROUNDS) || 12;
      const passwordHash = await bcrypt.hash(password, saltRounds);

      // Create user
      const user = await prisma.user.create({
        data: {
          email,
          passwordHash,
          firstName: first_name,
          lastName: last_name,
          phoneNumber: phone_number || null
        },
        select: {
          id: true,
          email: true,
          firstName: true,
          lastName: true,
          phoneNumber: true,
          emailVerified: true,
          phoneVerified: true,
          isActive: true,
          createdAt: true,
          updatedAt: true
        }
      });

      // Generate tokens
      const tokens = await this.generateTokens(user.id, user.email);

      logger.info(`New user registered: ${user.email}`);

      res.status(201).json({
        success: true,
        message: 'User created successfully',
        data: {
          user,
          tokens
        }
      });
    } catch (error) {
      logger.error('Signup error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Login user
  async login(req, res) {
    try {
      const { email, password } = req.body;

      // Find user
      const user = await prisma.user.findUnique({
        where: { email }
      });

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password'
        });
      }

      // Check if user is active
      if (!user.isActive) {
        return res.status(401).json({
          success: false,
          message: 'Account is deactivated'
        });
      }

      // Verify password
      const isPasswordValid = await bcrypt.compare(password, user.passwordHash);
      if (!isPasswordValid) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password'
        });
      }

      // Update last login
      await prisma.user.update({
        where: { id: user.id },
        data: { lastLoginAt: new Date() }
      });

      // Generate tokens
      const tokens = await this.generateTokens(user.id, user.email);

      // Return user data (excluding password)
      const userData = {
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber,
        profilePictureUrl: user.profilePictureUrl,
        emailVerified: user.emailVerified,
        phoneVerified: user.phoneVerified,
        isActive: user.isActive,
        createdAt: user.createdAt,
        updatedAt: user.updatedAt,
        lastLoginAt: user.lastLoginAt
      };

      logger.info(`User logged in: ${user.email}`);

      res.json({
        success: true,
        message: 'Login successful',
        data: {
          user: userData,
          tokens
        }
      });
    } catch (error) {
      logger.error('Login error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Refresh access token
  async refreshToken(req, res) {
    try {
      const { refresh_token } = req.body;

      // Verify refresh token
      const decoded = jwt.verify(refresh_token, process.env.JWT_REFRESH_SECRET);

      // Find token in database
      const tokenRecord = await prisma.token.findFirst({
        where: {
          refreshTokenHash: await bcrypt.hash(refresh_token, 10), // Hash to compare
          isRevoked: false,
          refreshTokenExpiresAt: {
            gt: new Date()
          }
        },
        include: {
          user: true
        }
      });

      if (!tokenRecord) {
        return res.status(401).json({
          success: false,
          message: 'Invalid or expired refresh token'
        });
      }

      // Check if user is still active
      if (!tokenRecord.user.isActive) {
        return res.status(401).json({
          success: false,
          message: 'Account is deactivated'
        });
      }

      // Revoke old token
      await prisma.token.update({
        where: { id: tokenRecord.id },
        data: {
          isRevoked: true,
          revokedAt: new Date()
        }
      });

      // Generate new tokens
      const newTokens = await this.generateTokens(tokenRecord.user.id, tokenRecord.user.email);

      logger.info(`Tokens refreshed for user: ${tokenRecord.user.email}`);

      res.json({
        success: true,
        message: 'Tokens refreshed successfully',
        data: {
          tokens: newTokens
        }
      });
    } catch (error) {
      if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
        return res.status(401).json({
          success: false,
          message: 'Invalid or expired refresh token'
        });
      }

      logger.error('Token refresh error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Logout user
  async logout(req, res) {
    try {
      const { refresh_token } = req.body;
      const userId = req.user.id;

      if (refresh_token) {
        // Revoke specific refresh token
        await prisma.token.updateMany({
          where: {
            userId,
            refreshTokenHash: await bcrypt.hash(refresh_token, 10),
            isRevoked: false
          },
          data: {
            isRevoked: true,
            revokedAt: new Date()
          }
        });
      } else {
        // Revoke all tokens for user
        await prisma.token.updateMany({
          where: {
            userId,
            isRevoked: false
          },
          data: {
            isRevoked: true,
            revokedAt: new Date()
          }
        });
      }

      logger.info(`User logged out: ${req.user.email}`);

      res.json({
        success: true,
        message: 'Logout successful'
      });
    } catch (error) {
      logger.error('Logout error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Get user profile
  async getProfile(req, res) {
    try {
      const user = await prisma.user.findUnique({
        where: { id: req.user.id },
        select: {
          id: true,
          email: true,
          firstName: true,
          lastName: true,
          phoneNumber: true,
          profilePictureUrl: true,
          emailVerified: true,
          phoneVerified: true,
          isActive: true,
          createdAt: true,
          updatedAt: true,
          lastLoginAt: true
        }
      });

      if (!user) {
        return res.status(404).json({
          success: false,
          message: 'User not found'
        });
      }

      res.json({
        success: true,
        message: 'Profile retrieved successfully',
        data: user
      });
    } catch (error) {
      logger.error('Get profile error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Update user profile
  async updateProfile(req, res) {
    try {
      const { first_name, last_name, phone_number } = req.body;
      const userId = req.user.id;

      const updateData = {};
      if (first_name) updateData.firstName = first_name;
      if (last_name) updateData.lastName = last_name;
      if (phone_number) updateData.phoneNumber = phone_number;

      const user = await prisma.user.update({
        where: { id: userId },
        data: updateData,
        select: {
          id: true,
          email: true,
          firstName: true,
          lastName: true,
          phoneNumber: true,
          profilePictureUrl: true,
          emailVerified: true,
          phoneVerified: true,
          isActive: true,
          createdAt: true,
          updatedAt: true,
          lastLoginAt: true
        }
      });

      logger.info(`Profile updated for user: ${user.email}`);

      res.json({
        success: true,
        message: 'Profile updated successfully',
        data: user
      });
    } catch (error) {
      logger.error('Update profile error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Change password
  async changePassword(req, res) {
    try {
      const { current_password, new_password } = req.body;
      const userId = req.user.id;

      // Get user with password hash
      const user = await prisma.user.findUnique({
        where: { id: userId }
      });

      if (!user) {
        return res.status(404).json({
          success: false,
          message: 'User not found'
        });
      }

      // Verify current password
      const isCurrentPasswordValid = await bcrypt.compare(current_password, user.passwordHash);
      if (!isCurrentPasswordValid) {
        return res.status(400).json({
          success: false,
          message: 'Current password is incorrect'
        });
      }

      // Hash new password
      const saltRounds = parseInt(process.env.BCRYPT_ROUNDS) || 12;
      const newPasswordHash = await bcrypt.hash(new_password, saltRounds);

      // Update password
      await prisma.user.update({
        where: { id: userId },
        data: { passwordHash: newPasswordHash }
      });

      // Revoke all existing tokens
      await prisma.token.updateMany({
        where: {
          userId,
          isRevoked: false
        },
        data: {
          isRevoked: true,
          revokedAt: new Date()
        }
      });

      logger.info(`Password changed for user: ${user.email}`);

      res.json({
        success: true,
        message: 'Password changed successfully. Please log in again.'
      });
    } catch (error) {
      logger.error('Change password error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Forgot password
  async forgotPassword(req, res) {
    try {
      const { email } = req.body;

      const user = await prisma.user.findUnique({
        where: { email }
      });

      if (!user) {
        // Don't reveal if email exists or not
        return res.json({
          success: true,
          message: 'If an account with that email exists, a password reset link has been sent.'
        });
      }

      // Generate reset token
      const resetToken = uuidv4();
      const expiresAt = new Date(Date.now() + 3600000); // 1 hour

      // Store reset token
      await prisma.passwordResetToken.create({
        data: {
          userId: user.id,
          token: resetToken,
          expiresAt
        }
      });

      // Send reset email
      await sendPasswordResetEmail(user.email, user.firstName, resetToken);

      logger.info(`Password reset requested for user: ${user.email}`);

      res.json({
        success: true,
        message: 'If an account with that email exists, a password reset link has been sent.'
      });
    } catch (error) {
      logger.error('Forgot password error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Reset password
  async resetPassword(req, res) {
    try {
      const { token, new_password } = req.body;

      // Find reset token
      const resetTokenRecord = await prisma.passwordResetToken.findUnique({
        where: { token },
        include: { user: true }
      });

      if (!resetTokenRecord || resetTokenRecord.isUsed || resetTokenRecord.expiresAt < new Date()) {
        return res.status(400).json({
          success: false,
          message: 'Invalid or expired reset token'
        });
      }

      // Hash new password
      const saltRounds = parseInt(process.env.BCRYPT_ROUNDS) || 12;
      const newPasswordHash = await bcrypt.hash(new_password, saltRounds);

      // Update password and mark token as used
      await prisma.$transaction([
        prisma.user.update({
          where: { id: resetTokenRecord.userId },
          data: { passwordHash: newPasswordHash }
        }),
        prisma.passwordResetToken.update({
          where: { id: resetTokenRecord.id },
          data: {
            isUsed: true,
            usedAt: new Date()
          }
        }),
        // Revoke all existing tokens
        prisma.token.updateMany({
          where: {
            userId: resetTokenRecord.userId,
            isRevoked: false
          },
          data: {
            isRevoked: true,
            revokedAt: new Date()
          }
        })
      ]);

      logger.info(`Password reset completed for user: ${resetTokenRecord.user.email}`);

      res.json({
        success: true,
        message: 'Password reset successfully. Please log in with your new password.'
      });
    } catch (error) {
      logger.error('Reset password error:', error);
      res.status(500).json({
        success: false,
        message: 'Internal server error'
      });
    }
  }

  // Generate access and refresh tokens
  async generateTokens(userId, email) {
    const accessToken = jwt.sign(
      { userId, email, type: 'access' },
      process.env.JWT_ACCESS_SECRET,
      { expiresIn: process.env.JWT_ACCESS_EXPIRES_IN || '15m' }
    );

    const refreshToken = jwt.sign(
      { userId, email, type: 'refresh' },
      process.env.JWT_REFRESH_SECRET,
      { expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d' }
    );

    // Calculate expiration dates
    const accessTokenExpiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15 minutes
    const refreshTokenExpiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000); // 7 days

    // Hash tokens for storage
    const accessTokenHash = await bcrypt.hash(accessToken, 10);
    const refreshTokenHash = await bcrypt.hash(refreshToken, 10);

    // Store tokens in database
    await prisma.token.create({
      data: {
        userId,
        accessTokenHash,
        refreshTokenHash,
        accessTokenExpiresAt,
        refreshTokenExpiresAt
      }
    });

    return {
      access_token: accessToken,
      refresh_token: refreshToken
    };
  }
}

module.exports = new AuthController();

