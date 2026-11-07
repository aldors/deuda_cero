const API_URL = "http://localhost:8080";

let promesaRefresco = null;


async function refrescarAccessToken() {

    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        const error = new Error("No existe un refresh token.");
        error.status = 401;
        throw error;
    }

    const response = await fetch(`${API_URL}/auth/refresh-token`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            refreshToken: refreshToken
        })

    });

    let data = null;

    try {
        data = await response.json();
    } catch (error) {
        data = null;
    }

    if (!response.ok || !data) {

        const mensaje = data && data.errors && Array.isArray(data.errors)
            ? data.errors.join(", ")
            : (data && data.message) || "No se pudo renovar la sesión.";

        const error = new Error(mensaje);
        error.status = 401;
        throw error;
    }

    localStorage.setItem("accessToken", data.accessToken);

    return data.accessToken;
}


async function obtenerAccessTokenRenovado() {

    if (!promesaRefresco) {

        promesaRefresco = refrescarAccessToken()
            .finally(() => {
                promesaRefresco = null;
            });
    }

    return promesaRefresco;
}


async function peticionAutenticada(endpoint, opciones = {}) {

    let accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        const error = new Error("No existe una sesión activa.");
        error.status = 401;
        throw error;
    }

    const headers = {
        ...opciones.headers,
        "Authorization": `Bearer ${accessToken}`
    };

    let response = await fetch(
        `${API_URL}${endpoint}`,
        {
            ...opciones,
            headers: headers
        }
    );


    if (response.status === 401) {

        try {

            accessToken = await obtenerAccessTokenRenovado();

        } catch (error) {

            cerrarSesionLocal();

            window.location.href = "index.html";

            throw error;
        }

        headers["Authorization"] = `Bearer ${accessToken}`;

        response = await fetch(
            `${API_URL}${endpoint}`,
            {
                ...opciones,
                headers: headers
            }
        );
    }


    return response;
}

function cerrarSesionLocal() {

    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
}

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

async function cerrarSesion() {

    const refreshToken = localStorage.getItem("refreshToken");

    try {

        if (refreshToken) {

            const response = await fetch(`${API_URL}/auth/logout`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    refreshToken: refreshToken
                })
            });

            if (!response.ok) {

                const data = await response.text();

                console.error("No se pudo cerrar la sesión correctamente:", data);
            }
        }

    } catch (error) {

        console.error("No se pudo conectar con el servidor:", error);

    } finally {

        cerrarSesionLocal();

        window.location.href = "index.html";
    }
}

async function obtenerUsuarioActual() {

    const response = await peticionAutenticada("/auth/me");

    if (!response.ok) {

        const error = new Error("No se pudo obtener la información del usuario");

        error.status = response.status;

        throw error;
    }

    return response.json();
}

function formatearFecha(fecha, incluirHora = false) {

    const fechaObj = new Date(fecha);

    const opciones = {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    };

    if (incluirHora) {

        opciones.hour = "2-digit";
        opciones.minute = "2-digit";
        opciones.second = "2-digit";
    }

    return fechaObj.toLocaleString("es-MX", opciones);
}

function configurarMenuMovil() {

    const sidebar = document.getElementById("sidebar");
    const menuToggle = document.getElementById("menuToggle");
    const overlay = document.getElementById("sidebarOverlay");

    if (!sidebar || !menuToggle || !overlay) {
        return;
    }

    const abrirMenu = () => {

        sidebar.classList.add("open");
        overlay.classList.remove("hidden");
        document.body.classList.add("menu-open");
        menuToggle.setAttribute("aria-expanded", "true");
        menuToggle.setAttribute("aria-label", "Cerrar menú");
    };

    const cerrarMenu = () => {

        sidebar.classList.remove("open");
        overlay.classList.add("hidden");
        document.body.classList.remove("menu-open");
        menuToggle.setAttribute("aria-expanded", "false");
        menuToggle.setAttribute("aria-label", "Abrir menú");
    };

    menuToggle.addEventListener("click", () => {

        if (sidebar.classList.contains("open")) {
            cerrarMenu();
        } else {
            abrirMenu();
        }
    });

    overlay.addEventListener("click", cerrarMenu);

    sidebar.querySelectorAll("a, button").forEach(elemento => {

        elemento.addEventListener("click", cerrarMenu);
    });

    document.addEventListener("keydown", event => {

        if (event.key === "Escape") {
            cerrarMenu();
        }
    });

    window.addEventListener("resize", () => {

        if (window.innerWidth > 800) {
            cerrarMenu();
        }
    });
}
