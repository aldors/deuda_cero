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

async function obtenerUsuarioActual() {

    const response = await peticionAutenticada("/auth/me");

    if (!response.ok) {
        throw new Error("No se pudo obtener la información del usuario");
    }

    return response.json();
}

const logout = document.getElementById("logoutBtn");

logout.addEventListener("click", async () => {

    await cerrarSesion();

    console.log("Tokens después del logout:");
    console.log("Access:", localStorage.getItem("accessToken"));
    console.log("Refresh:", localStorage.getItem("refreshToken"));

});

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
        window.location.href = "index.html";

    } catch (error) {

        console.error("No se pudo conectar con el servidor:", error);

    } finally {

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    }
}


async function obtenerMisGrupos() {

    const response = await peticionAutenticada("/grupos/grupos");

    if (!response.ok) {
        throw new Error("No se pudieron obtener los grupos");
    }

    return response.json();
}

async function obtenerInvitaciones() {

    const response = await peticionAutenticada(
        "/invitaciones/invitaciones"
    );

    if (!response.ok) {
        throw new Error("No se pudieron obtener las invitaciones");
    }

    return response.json();
}


async function cargarUsuario() {

    const usuario = await obtenerUsuarioActual();

    document.getElementById("userName").textContent =
        usuario.nombre;

    document.getElementById("userNameHeader").textContent =
        usuario.nombre;

    document.getElementById("userEmail").textContent =
        usuario.email;

    document.getElementById("userAvatar").textContent =
        usuario.nombre.charAt(0).toUpperCase();
}


async function cargarGrupos() {

    const container = document.getElementById("groupsContainer");

    try {

        const grupos = await obtenerMisGrupos();

        container.innerHTML = "";

        if (grupos.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">▣</div>
                    <p>Aún no perteneces a ningún grupo.</p>
                </div>
            `;

            return;
        }

        grupos.forEach(grupo => {

            const card = document.createElement("div");

            card.classList.add("group-card");

            card.innerHTML = `
                <div class="group-card-top">

                    <div class="group-icon">
                        $
                    </div>

                    <span class="group-arrow">
                        →
                    </span>

                </div>

                <h3>${grupo.nombre}</h3>

                <p class="group-description">
                    ${grupo.descripcion || "Sin descripción"}
                </p>

                <span class="group-members">
                    ${grupo.totalMiembros}
                    ${grupo.totalMiembros === 1 ? "miembro" : "miembros"}
                </span>
            `;

            card.addEventListener("click", () => {

                window.location.href = `grupo-dashboard.html?grupoId=${grupo.id}`;

                //Eso lo hice yo y no se si sea correcto para mostrar el nombre del
                //grupo en el dashboard del grupo
                localStorage.setItem("nombreGrupo", grupo.nombre);

            });

            container.appendChild(card);
        });

    } catch (error) {

        console.error("Error al cargar grupos:", error);

        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">!</div>
                <p>No se pudieron cargar tus grupos.</p>
            </div>
        `;
    }
}

function mostrarInvitaciones(invitaciones) {

    const container =
        document.getElementById("invitationsContainer");

    const badge =
        document.querySelector(".notification-badge");

    container.innerHTML = "";

    /*
     * Actualizar cantidad del badge
     */
    if (badge) {

        if (invitaciones.length > 0) {

            badge.textContent = invitaciones.length;
            badge.classList.remove("hidden");

        } else {

            badge.classList.add("hidden");
        }
    }


    /*
     * No hay invitaciones
     */
    if (invitaciones.length === 0) {

        container.innerHTML = `
            <div class="empty-state">

                <div class="empty-icon">
                    ✉
                </div>

                <p>
                    No tienes invitaciones pendientes.
                </p>

            </div>
        `;

        return;
    }


    /*
     * Crear tarjetas
     */
    invitaciones.forEach(invitacion => {

        const card =
            document.createElement("div");

        card.classList.add("invitation-card");

        card.innerHTML = `
            <div class="invitation-info">

                <strong>
                    ${invitacion.grupo}
                </strong>

                <span>
                    Invitación de ${invitacion.invitador}
                </span>

                <span>
                    Fecha de envio: ${formatearFecha(invitacion.fechaEnvio)}
                </span>

            </div>

            <div class="invitation-actions">

                <button
                    class="accept-button"
                    data-id="${invitacion.id}"
                >
                    Aceptar
                </button>

                <button
                    class="reject-button"
                    data-id="${invitacion.id}"
                >
                    Rechazar
                </button>

            </div>
        `;


        /*
         * Botón aceptar
         */
        card
            .querySelector(".accept-button")
            .addEventListener("click", () => {

                aceptarInvitacion(invitacion.id);
            });


        /*
         * Botón rechazar
         */
        card
            .querySelector(".reject-button")
            .addEventListener("click", () => {

                rechazarInvitacion(invitacion.id);
            });


        container.appendChild(card);
    });
}

//Prueba para que no aparezca feo en la invitación
function formatearFecha(fecha) {

    const fechaObj = new Date(fecha);

    return fechaObj.toLocaleDateString("es-MX", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });
}

async function cargarInvitaciones() {

    const container =
        document.getElementById("invitationsContainer");

    try {

        const invitaciones =
            await obtenerInvitaciones();

        console.log(
            "Invitaciones pendientes:",
            invitaciones
        );

        mostrarInvitaciones(invitaciones);

    } catch (error) {

        console.error(
            "Error al cargar invitaciones:",
            error
        );

        container.innerHTML = `
            <div class="empty-state">

                <div class="empty-icon">
                    !
                </div>

                <p>
                    No se pudieron cargar las invitaciones.
                </p>

            </div>
        `;
    }
}

async function aceptarInvitacion(invitacionId) {

    try {

        const response = await peticionAutenticada(
            `/invitaciones/invitaciones/${invitacionId}/aceptar`,
            {
                method: "POST"
            }
        );


        const data = await response.json();


        if (!response.ok) {

            throw new Error(
                data.message ||
                "No se pudo aceptar la invitación"
            );
        }


        console.log(
            "Invitación aceptada:",
            data
        );


        /*
         * Volver a cargar grupos porque
         * ahora pertenecemos al nuevo grupo.
         */
        await cargarGrupos();


        /*
         * Volver a cargar invitaciones
         * para quitar la invitación aceptada.
         */
        await cargarInvitaciones();


    } catch (error) {

        console.error(
            "Error al aceptar invitación:",
            error
        );

        alert(error.message);
    }
}

async function rechazarInvitacion(invitacionId) {

    try {

        const response = await peticionAutenticada(
            `/invitaciones/invitaciones/${invitacionId}/rechazar`,
            {
                method: "POST"
            }
        );


        const data = await response.json();


        if (!response.ok) {

            throw new Error(
                data.message ||
                "No se pudo rechazar la invitación"
            );
        }


        console.log(
            "Invitación rechazada:",
            data
        );


        /*
         * Actualizar la lista de invitaciones
         * y el contador.
         */
        await cargarInvitaciones();


    } catch (error) {

        console.error(
            "Error al rechazar invitación:",
            error
        );

        alert(error.message);
    }
}

function abrirModalCrearGrupo() {

    document.getElementById("createGroupModal")
        .classList.remove("hidden");
}


function cerrarModalCrearGrupo() {

    document.getElementById("createGroupModal")
        .classList.add("hidden");

    document.getElementById("createGroupForm").reset();

    document.getElementById("createGroupError")
        .classList.add("hidden");

    document.getElementById("createGroupError")
        .textContent = "";
}


async function crearGrupo(event) {

    event.preventDefault();

    const nombre = document.getElementById("groupName").value.trim();

    const descripcion = document.getElementById("groupDescription").value.trim();

    const errorElement = document.getElementById("createGroupError");

    errorElement.classList.add("hidden");

    try {

        const response = await peticionAutenticada("/grupos/crear", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                nombre: nombre,
                descripcion: descripcion || null
            })
        });


        if (!response.ok ) {

            const error = await response.json();

            // throw new Error(
            //     error.message || "No se pudo crear el grupo"
            // );

            throw error;

        }


        cerrarModalCrearGrupo();

        await cargarGrupos();


    } catch (error) {

        console.error("Error al crear grupo:", error);

        //errorElement.textContent = error.message;
        mostrarErrores(errorElement, error.errors || error.message);

        errorElement.classList.remove("hidden");
    }
}

document
    .getElementById("createGroupBtn")
    .addEventListener("click", abrirModalCrearGrupo);


document
    .getElementById("closeModalBtn")
    .addEventListener("click", cerrarModalCrearGrupo);


document
    .getElementById("cancelModalBtn")
    .addEventListener("click", cerrarModalCrearGrupo);


document
    .getElementById("createGroupForm")
    .addEventListener("submit", crearGrupo);


async function cargarDashboard() {

    try {

        await cargarUsuario();

        await cargarGrupos();

        await cargarInvitaciones();

    } catch (error) {

        console.error("Error al cargar el dashboard:", error);

        cerrarSesionLocal();

        window.location.href = "index.html";
    }
}


cargarDashboard();