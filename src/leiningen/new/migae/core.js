// {{ns.sym}}.{{project}}.core.js

function hello () {
    alert("hello, world")
}

window.onload = function() {
    document.getElementById("hello").addEventListener('click', hello);
}
