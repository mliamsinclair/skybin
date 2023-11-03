document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const credentials = {
        username: username,
        password: password
    };

    fetch('/api/authenticate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
    })
    .then(response => response.text())
    .then(jwtToken => {
        // Handle JWT token (e.g., store it in localStorage for future requests)
        console.log('Received JWT token:', jwtToken);
    })
    .catch(error => console.error('Error:', error));
});
