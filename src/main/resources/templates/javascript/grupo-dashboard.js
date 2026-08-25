let deudasPendientes = [];

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


async function obtenerDashboardGrupo(grupoId) {

    const response = await peticionAutenticada(
        `/grupos/${grupoId}/dashboard`
    );

    return response.json();
}

async function obtenerMiembrosGrupo(grupoId) {

    const response = await peticionAutenticada(
        `/grupos/${grupoId}/miembros`
    );

    if (!response.ok) {
        throw new Error("No se pudieron obtener los miembros del grupo");
    }

    return response.json();
}

async function registrarPago(grupoId, deudaId, monto) {

    const response = await peticionAutenticada(
        `/pagos/${grupoId}/registrar`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                deudaId,
                monto
            })
        }
    );

    if (!response.ok) {

        const error = await response.json();

        throw new Error(
            error.message || "No se pudo registrar el pago"
        );
    }
}

function configurarModalPago() {

    const modal =
        document.getElementById("registerPaymentModal");

    const abrirBtn =
        document.getElementById("registerPaymentBtn");

    const cerrarBtn =
        document.getElementById("closePaymentModalBtn");

    const cancelarBtn =
        document.getElementById("cancelPaymentModalBtn");

    const form =
        document.getElementById("registerPaymentForm");

    const deudaSelect =
        document.getElementById("paymentDebt");

    const montoInput =
        document.getElementById("paymentAmount");

    const errorContainer =
        document.getElementById("paymentError");


    function abrirModal() {

        deudaSelect.innerHTML = "";

        if (deudasPendientes.length === 0) {

            deudaSelect.innerHTML =
                "<option>No tienes deudas pendientes</option>";
        }
        else {

            deudasPendientes.forEach(deuda => {

                const option =
                    document.createElement("option");

                option.value = deuda.deudaId;

                option.textContent =
                    `${deuda.nombre} - ${formatearMonto(deuda.montoPendiente)}`;

                deudaSelect.appendChild(option);
            });
        }

        montoInput.value = "";

        errorContainer.classList.add("hidden");

        modal.classList.remove("hidden");
    }


    function cerrarModal() {

        modal.classList.add("hidden");
    }


    abrirBtn.addEventListener(
        "click",
        abrirModal
    );

    cerrarBtn.addEventListener(
        "click",
        cerrarModal
    );

    cancelarBtn.addEventListener(
        "click",
        cerrarModal
    );


    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        try {

            const grupoId = obtenerGrupoId();

            await registrarPago(
                grupoId,
                Number(deudaSelect.value),
                Number(montoInput.value)
            );

            cerrarModal();

            await cargarDashboardGrupo();

        }
        catch(error) {

            console.error(error);

            errorContainer.textContent =
                error.message;

            errorContainer.classList.remove("hidden");
        }

    });
}

async function invitarMiembro(grupoId, email) {

    const response = await peticionAutenticada(
        `/invitaciones/invitar/${grupoId}`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email
            })
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "No se pudo enviar la invitación");
    }

    return data;
}

function configurarModalInvitacion() {

    const modal = document.getElementById("inviteMemberModal");

    const abrirBtn = document.getElementById("inviteMemberBtn");

    const cerrarBtn = document.getElementById("closeInviteModalBtn");

    const cancelarBtn = document.getElementById("cancelInviteModalBtn");

    const form = document.getElementById("inviteMemberForm");

    const emailInput = document.getElementById("memberEmail");

    const errorContainer = document.getElementById("inviteMemberError");


    function abrirModal() {

        modal.classList.remove("hidden");

        emailInput.value = "";

        errorContainer.classList.add("hidden");

        errorContainer.textContent = "";

        emailInput.focus();
    }


    function cerrarModal() {

        modal.classList.add("hidden");
    }


    abrirBtn.addEventListener("click", abrirModal);

    cerrarBtn.addEventListener("click", cerrarModal);

    cancelarBtn.addEventListener("click", cerrarModal);


    form.addEventListener("submit", async function(event) {

        event.preventDefault();


        const grupoId = obtenerGrupoId();

        const email = emailInput.value.trim();


        if (!grupoId) {

            errorContainer.textContent =
                "No se pudo identificar el grupo.";

            errorContainer.classList.remove("hidden");

            return;
        }


        try {

            const invitacion = await invitarMiembro(
                grupoId,
                email
            );


            console.log(
                "Invitación enviada:",
                invitacion
            );


            cerrarModal();

        } catch (error) {

            console.error("Error al enviar invitación:", error);

            errorContainer.textContent = error.message;

            errorContainer.classList.remove("hidden");

        }

    });
}

function obtenerGrupoId() {

    const params = new URLSearchParams(window.location.search);

    return params.get("grupoId");
}


function formatearMonto(monto) {

    return new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN"
    }).format(monto);
}


function formatearFecha(fecha) {

    const fechaObj = new Date(fecha);

    return fechaObj.toLocaleDateString("es-MX", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    });
}


function mostrarDeudas(deudas, contenedorId, tipo) {

    const contenedor = document.getElementById(contenedorId);

    contenedor.innerHTML = "";

    if (!deudas || deudas.length === 0) {

        contenedor.innerHTML = `
            <div class="empty-state">
                <p>
                    ${tipo === "debes"
                        ? "No tienes deudas pendientes."
                        : "Nadie te debe dinero."
                    }
                </p>
            </div>
        `;

        return;
    }


    deudas.forEach(deuda => {

        const tarjeta = document.createElement("div");

        tarjeta.className = "debt-card";

        tarjeta.innerHTML = `
            <div class="debt-info">

                <strong>
                    ${deuda.nombre}
                </strong>

                <span>
                    ${tipo === "debes"
                        ? "Le debes"
                        : "Te debe"
                    }
                </span>

            </div>

            <span class="debt-amount">
                ${formatearMonto(deuda.montoPendiente)}
            </span>
        `;

        contenedor.appendChild(tarjeta);

    });
}


function mostrarMovimientos(movimientos) {

    const contenedor = document.getElementById("recentMovements");

    contenedor.innerHTML = "";

    if (!movimientos || movimientos.length === 0) {

        contenedor.innerHTML = `
            <div class="empty-state">
                <p>
                    No hay movimientos recientes.
                </p>
            </div>
        `;

        return;
    }


    movimientos.forEach(movimiento => {

        const tarjeta = document.createElement("div");

        tarjeta.className = "movement-card";

        tarjeta.innerHTML = `
            <div class="movement-info">

                <strong>
                    ${movimiento.descripcion}
                </strong>

                <span>
                    Pagado por ${movimiento.pagador}
                    · ${formatearFecha(movimiento.fechaMovimiento)}
                </span>

            </div>

            <span class="movement-amount">
                ${formatearMonto(movimiento.montoTotal)}
            </span>
        `;

        contenedor.appendChild(tarjeta);

    });
}

function cargarMiembrosEnFormulario(miembros) {

    const selectPagador = document.getElementById("movementPayer"); ///

    const participantesContainer =
        document.getElementById("participantsContainer"); ///


    selectPagador.innerHTML = `
        <option value="">
            Selecciona un miembro
        </option>
    `;

    participantesContainer.innerHTML = "";


    miembros.forEach(miembro => {

        // PAGADOR

        const opcion = document.createElement("option");

        opcion.value = miembro.id;

        opcion.textContent = miembro.nombre;

        selectPagador.appendChild(opcion);


        // PARTICIPANTE

        const participante = document.createElement("div");

        participante.className = "participant-option";

        participante.innerHTML = `

            <div class="participant-main">

                <input
                    type="checkbox"
                    name="participantes"
                    value="${miembro.id}"
                    checked
                >

                <span>
                    ${miembro.nombre}
                </span>

            </div>


            <input
                type="number"
                class="participant-amount hidden"
                data-usuario-id="${miembro.id}"
                placeholder="$0.00"
                min="0.01"
                step="0.01"
            >

        `;

        participantesContainer.appendChild(participante);

    });
}

function cambiarTipoDivision() {

    const tipoDivision =
        document.getElementById("movementDivision").value;

    const camposMonto =
        document.querySelectorAll(".participant-amount");


    camposMonto.forEach(campo => {

        if (tipoDivision === "PERSONALIZADA") {

            campo.classList.remove("hidden");

            campo.required = true;

        } else {

            campo.classList.add("hidden");

            campo.required = false;

            campo.value = "";

        }

    });
}

function abrirModalMovimiento() {

    document
        .getElementById("createMovementModal")
        .classList.remove("hidden");
}


function cerrarModalMovimiento() {

    document
        .getElementById("createMovementModal")
        .classList.add("hidden");
}


async function cargarDashboardGrupo() {

    try {

       await obtenerUsuarioActual();

        const grupoId = obtenerGrupoId();

        if (!grupoId) {
            throw new Error("No se recibió el ID del grupo");
        }

        //Aqui muestro el nombre del grupo que esta guardado en el localStorage
        document.getElementById("groupName").textContent = localStorage.getItem("nombreGrupo");

        /*
         * Obtener información del usuario
         */

        const usuario = await obtenerUsuarioActual();

        document.getElementById("userNameHeader").textContent =
            usuario.nombre;


        document.getElementById("userEmail").textContent =
            usuario.email;


        document.getElementById("userAvatar").textContent =
            usuario.nombre.charAt(0).toUpperCase();


        /*
         * Obtener información del grupo
         */

        const dashboard = await obtenerDashboardGrupo(grupoId);

        deudasPendientes = dashboard.debes || [];

        console.log("Dashboard del grupo:", dashboard);

        const miembros = await obtenerMiembrosGrupo(grupoId);
        console.log("Miembros del grupo:", miembros);
        cargarMiembrosEnFormulario(miembros);


        /*
         * BALANCE
         */

        document.getElementById("balanceGeneral").textContent =
            formatearMonto(dashboard.balanceGeneral);


        /*
         * DEUDAS
         */

        mostrarDeudas(
            dashboard.debes,
            "debtsYouOwe",
            "debes"
        );


        mostrarDeudas(
            dashboard.teDeben,
            "debtsTheyOwe",
            "teDeben"
        );


        /*
         * MOVIMIENTOS
         */

        mostrarMovimientos(
            dashboard.movimientosRecientes
        );


    } catch (error) {

        console.error("Error al cargar el dashboard del grupo:", error);

        //No deberiamos cerrar sesion ante cualquier error -> cerrarSesionLocal();

        window.location.href = "index.html"

    }
}

function obtenerParticipantesFormulario() {

    const participantes = [];

    const checkboxes =
        document.querySelectorAll(
            '#participantsContainer input[name="participantes"]:checked'
        );


    checkboxes.forEach(checkbox => {

        const usuarioId = Number(checkbox.value);

        let monto = 0;


        if (
            document.getElementById("movementDivision").value
            === "PERSONALIZADA"
        ) {

            const campoMonto =
                document.querySelector(
                    `.participant-amount[data-usuario-id="${usuarioId}"]`
                );

            monto = Number(campoMonto.value);

        }


        participantes.push({
            usuarioId: usuarioId,
            monto: monto
        });

    });


    return participantes;
}

function construirMovimientoRequest() {

    const descripcion =
        document.getElementById("movementDescription").value.trim();

    const montoTotal =
        Number(document.getElementById("movementAmount").value);

    const pagadorId =
        Number(document.getElementById("movementPayer").value);
    
    //const tipoMovimiento = document.getElementById("movementType").value;

    const tipoDivision =
        document.getElementById("movementDivision").value;

    const participantes =
        obtenerParticipantesFormulario();

    // Lo puedo quitar cuando use los errores del backend
    // if (participantes.length === 0) {

    //     throw new Error(
    //         "Debe seleccionar al menos un participante"
    //     );
    // }


    return {
        descripcion: descripcion,
        montoTotal: montoTotal,
        pagadorId: pagadorId,
        tipoMovimiento: "GASTO", // Se pone asi ya que siempre sera de tipo GASTO
        tipoDivision: tipoDivision,
        participantes: participantes
    };
}

async function crearMovimiento() {

    try {

        const grupoId = obtenerGrupoId();

        if (!grupoId) {
            throw new Error("No se recibió el ID del grupo");
        }


        const request = construirMovimientoRequest();

        console.log("Request para crear movimiento:", request);


        const response = await peticionAutenticada(
            `/movimientos/crear/${grupoId}`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(request)
            }
        );


        if (!response.ok) {

            const errorData = await response.json();

            // throw new Error(
            //     errorData.message ||
            //     "No se pudo crear el movimiento"
            // );

            throw errorData;
        }


        const movimiento = await response.json();

        console.log( "Movimiento creado correctamente:", movimiento );

        cerrarModalMovimiento();

        document.getElementById("createMovementForm").reset();

        cargarDashboardGrupo();


    } catch (error) {

        console.error("Error al crear movimiento:", error);

        const errorContainer = document.getElementById("createMovementError");

        //errorContainer.textContent = error.message;
        mostrarErrores(errorContainer, error.errors || error.message)

        errorContainer.classList.remove("hidden");

    }
}

document
    .getElementById("createMovementBtn")
    .addEventListener("click", abrirModalMovimiento);


document
    .getElementById("closeMovementModalBtn")
    .addEventListener("click", cerrarModalMovimiento);


document
    .getElementById("cancelMovementBtn")
    .addEventListener("click", cerrarModalMovimiento);

document
    .getElementById("movementDivision")
    .addEventListener("change", cambiarTipoDivision);

document
    .getElementById("createMovementForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        crearMovimiento();

    });


    //Definir que falta tipo movimiento y que solo hay gasto, ademas decirle que hagamos el cosumo de la 
    //api para poder crear el movimiento y especificarle que cuando sea personalizada debemos tener
    //un apartado para agregarles el monto y que el js se tome el id y el monto del usuario y se mande como lista
    //de participantes y cuando sea igual pues se ponga 0 por defeto o algun valor ya que ahi no importa

//Ver info del movimiento en la consola del navegador
/*document
    .getElementById("createMovementForm")
    .addEventListener("submit", function (event) {

        event.preventDefault();

        const descripcion =
            document.getElementById("movementDescription").value;

        const monto =
            document.getElementById("movementAmount").value;

        const pagadorId =
            document.getElementById("movementPayer").value;

        const division =
            document.querySelector(
                'input[name="divisionType"]:checked'
            ).value;

        const participantes =
            Array.from(
                document.querySelectorAll(
                    'input[name="participantes"]:checked'
                )
            ).map(input => Number(input.value));


        console.log("Descripción:", descripcion);
        console.log("Monto:", monto);
        console.log("Pagador:", pagadorId);
        console.log("División:", division);
        console.log("Participantes:", participantes);

    });
*/

configurarModalInvitacion();
configurarModalPago();
cargarDashboardGrupo();