const API_URL = "http://localhost:8080";


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


// Entender como funciona este metodo
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

        accessToken = await refrescarAccessToken();

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

//async ?
async function cerrarSesionLocal() {

    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
}