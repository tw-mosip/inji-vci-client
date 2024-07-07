const express = require('express');
const path = require('path');
const cors = require('cors'); // Require the 'cors' module
const app = express();

app.use(cors()); // Enable CORS for all routes
app.use(express.json());

app.use(express.static(path.join(__dirname, 'public')));

app.get('/getvc', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Endpoint to handle OAuth2 redirect
app.get('/oauthredirect', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// POST endpoint to handle calculations
app.post('/calculate', (req, res) => {
    const { num1, num2 } = req.body;
    const result = num1 + num2; // Example calculation
    res.json({ result });
});

module.exports = app;
