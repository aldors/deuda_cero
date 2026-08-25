function mostrarErrores(elemento, errores) {

    elemento.innerHTML = "";

    if (Array.isArray(errores)) {

        const lista = document.createElement("ul");

        errores.forEach(mensaje => {

            const item = document.createElement("li");

            item.textContent = mensaje;

            lista.appendChild(item);

        });

        elemento.appendChild(lista);

    } else {

        elemento.textContent = errores;
    }
}

// Este es un test de que funciona el boton de cerrar sesion
/*
const logoutTest = document.getElementById("logoutTest");

logoutTest.addEventListener("click", async () => {

    await cerrarSesion();

    console.log("Tokens después del logout:");
    console.log("Access:", localStorage.getItem("accessToken"));
    console.log("Refresh:", localStorage.getItem("refreshToken"));

});
*/

/*
- Este fargmento de codigo que esta debajo, tiene la funcion de hacer dinamido el index.html
    ya que en el index.html esta fusionado registro y login, entonces este codigo permite
    mostrar el registro cuando se desee registrar y ocultar temporalmente login y viceversa.
*/

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

/*
- Este fragmento de codigo hace el fetch/conexion/consumo de API
    POST /auth/registro
*/

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

        const response = await fetch("http://localhost:8080/auth/registro", {
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

/*
- Este fragmento de codigo hace el fetch/conexion/consumo de API
    POST /auth/login
*/

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

        const response = await fetch("http://localhost:8080/auth/login", {
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

/*
- Este fragmento de codigo hace el fetch/conexion/consumo de API
    POST /auth/logout
*/

async function cerrarSesion() {

    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        return;
    }

    try {

        const response = await fetch(`${API_URL}/auth/logout`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                refreshToken: refreshToken
            })

        });

        const data = await response.text();

        if (!response.ok) {

            console.error("No se pudo cerrar la sesión correctamente:", data);

            return;
        }

        console.log("Sesión cerrada correctamente.");

    } catch (error) {

        console.error("No se pudo conectar con el servidor:", error);

    } finally {

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    }
}