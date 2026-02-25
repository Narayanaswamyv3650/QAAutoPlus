// QA Auto Plus - Main JavaScript

/**
 * Test API connection
 */
function testApi() {
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = '<p>Testing API connection...</p>';
    resultDiv.className = 'show';

    fetch('/api/status')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            displayResult(data, 'success');
        })
        .catch(error => {
            displayResult({ error: error.message }, 'error');
        });
}

/**
 * Display API result
 */
function displayResult(data, type) {
    const resultDiv = document.getElementById('result');
    resultDiv.className = `show ${type}`;

    const message = type === 'success'
        ? '<h3>✓ API Connection Successful</h3>'
        : '<h3>✗ API Connection Failed</h3>';

    resultDiv.innerHTML = `
        ${message}
        <pre>${JSON.stringify(data, null, 2)}</pre>
    `;
}

/**
 * Initialize application
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('QA Auto Plus application loaded');

    // Add any initialization code here
});

