const API_URL = "http://localhost:8080";


async function refrescarAccessToken() {

    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        throw new Error("No existe un refresh token.");
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

    const data = await response.json();

    if (!response.ok) {

        if (data.errors && Array.isArray(data.errors)) {
            throw new Error(data.errors.join(", "));
        }

        throw new Error(
            data.message || "No se pudo renovar la sesión."
        );
    }

    localStorage.setItem("accessToken", data.accessToken);

    return data.accessToken;
}


// Entender como funciona este metodo
async function peticionAutenticada(endpoint, opciones = {}) {

    let accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        throw new Error("No existe una sesión activa.");
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