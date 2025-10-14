const express = require('express');
const { authenticateToken, requireOwnership } = require('../middleware/auth');
const { validatePagination, validateUUID } = require('../middleware/validation');

const router = express.Router();

// All routes require authentication
router.use(authenticateToken);

// Get user profile (protected)
router.get('/profile', (req, res) => {
  res.json({
    success: true,
    message: 'Profile retrieved successfully',
    data: {
      id: req.user.id,
      email: req.user.email,
      firstName: req.user.firstName,
      lastName: req.user.lastName
    }
  });
});

// Get user by ID (protected, own resources only)
router.get('/:id', validateUUID('id'), requireOwnership('id'), (req, res) => {
  res.json({
    success: true,
    message: 'User retrieved successfully',
    data: {
      id: req.user.id,
      email: req.user.email,
      firstName: req.user.firstName,
      lastName: req.user.lastName
    }
  });
});

// Update user profile (protected, own resources only)
router.put('/:id', validateUUID('id'), requireOwnership('id'), (req, res) => {
  // This would typically update the user in the database
  res.json({
    success: true,
    message: 'User updated successfully',
    data: {
      id: req.user.id,
      email: req.user.email,
      firstName: req.user.firstName,
      lastName: req.user.lastName
    }
  });
});

// Delete user account (protected, own resources only)
router.delete('/:id', validateUUID('id'), requireOwnership('id'), (req, res) => {
  // This would typically deactivate the user account
  res.json({
    success: true,
    message: 'User account deactivated successfully'
  });
});

module.exports = router;

