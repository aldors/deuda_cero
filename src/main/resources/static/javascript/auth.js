const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

const showRegister = document.getElementById("showRegister");
const showLogin = document.getElementById("showLogin");


showRegister.addEventListener("click", () => {

    loginForm.classList.remove("active");
    registerForm.classList.add("active");

});


showLogin.addEventListener("click", () => {

    registerForm.classList.remove("active");
    loginForm.classList.add("active");

});

const formRegister = document.getElementById("formRegister");
const registerMessage = document.getElementById("registerMessage");

formRegister.addEventListener("submit", async (event) => {

    event.preventDefault();

    const nombre = document.getElementById("registerNombre").value;
    const apellido = document.getElementById("registerApellido").value;
    const email = document.getElementById("registerEmail").value;
    const password = document.getElementById("registerPassword").value;

    const usuario = {
        nombre: nombre,
        apellido: apellido,
        email: email,
        password: password
    };

    try {

        const response = await fetch(`${API_URL}/auth/registro`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(usuario)
        });

        const data = await response.json();

        if (!response.ok) {

            if(data.errors && Array.isArray(data.errors)){
                mostrarErrores(registerMessage, data.errors);
            }
            else{
                mostrarErrores(registerMessage, data.message || "No se pudo crear la cuenta");
            }
        }
        else{
            registerMessage.textContent = "Cuenta creada correctamente.";
            formRegister.reset();
            console.log(data);
        }

    } catch (error) {

        registerMessage.textContent = error.message;
        console.error(error);
    }

});

const formLogin = document.getElementById("formLogin");
const loginMessage = document.getElementById("loginMessage");


formLogin.addEventListener("submit", async (event) => {

    event.preventDefault();

    loginMessage.innerHTML = "";

    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    const loginRequest = {
        email: email,
        password: password
    };

    try {

        const response = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(loginRequest)

        });

        const data = await response.json();

        if (!response.ok) {

            if (data.errors && Array.isArray(data.errors)) {

                mostrarErrores(loginMessage, data.errors);

            } else {

                mostrarErrores(loginMessage, data.message || "No se pudo iniciar sesión.");
            }

            return;
        }

        console.log("Login exitoso");
        console.log(data);

        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);

        loginMessage.textContent = "Inicio de sesión exitoso.";

        window.location.href = "dashboard.html";

    } catch (error) {

        loginMessage.textContent = "No se pudo conectar con el servidor.";
        console.error(error);
    }

});

if (localStorage.getItem("accessToken")) {
    window.location.href = "dashboard.html";
}
