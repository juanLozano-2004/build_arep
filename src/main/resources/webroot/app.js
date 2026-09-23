function greet() {
    const name = document.getElementById("name").value;

    fetch("/hello?name=" + encodeURIComponent(name))
        .then(response => response.text())
        .then(message => {
            document.getElementById("result").innerHTML = message;
        })
        .catch(error => {
            document.getElementById("result").innerHTML = "Error: " + error;
        });
}