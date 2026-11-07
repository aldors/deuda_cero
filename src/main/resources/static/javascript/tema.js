(function () {

    const CLAVE_TEMA = "tema";
    const TEMA_OSCURO = "oscuro";
    const TEMA_CLARO = "claro";

    const preferenciaOscura = window.matchMedia("(prefers-color-scheme: dark)");

    const obtenerTemaGuardado = () => {

        try {
            return localStorage.getItem(CLAVE_TEMA);
        } catch (error) {
            return null;
        }
    };

    const guardarTema = (tema) => {

        try {
            localStorage.setItem(CLAVE_TEMA, tema);
        } catch (error) {
        }
    };

    const temaGuardado = obtenerTemaGuardado();
    const usarTemaOscuro = temaGuardado === TEMA_OSCURO
        || (temaGuardado !== TEMA_CLARO && preferenciaOscura.matches);

    if (usarTemaOscuro) {
        document.documentElement.dataset.theme = TEMA_OSCURO;
    }

    const configurarTema = () => {

        const botones = document.querySelectorAll("[data-theme-toggle]");

        if (botones.length === 0) {
            return;
        }

        const actualizarBotones = () => {

            const esOscuro = document.documentElement.dataset.theme === TEMA_OSCURO;

            botones.forEach(boton => {

                boton.setAttribute("aria-pressed", String(esOscuro));
                boton.setAttribute(
                    "title",
                    esOscuro ? "Activar modo claro" : "Activar modo oscuro"
                );
            });
        };

        botones.forEach(boton => {

            boton.addEventListener("click", () => {

                const esOscuro = document.documentElement.dataset.theme === TEMA_OSCURO;

                if (esOscuro) {
                    delete document.documentElement.dataset.theme;
                    guardarTema(TEMA_CLARO);
                } else {
                    document.documentElement.dataset.theme = TEMA_OSCURO;
                    guardarTema(TEMA_OSCURO);
                }

                actualizarBotones();
            });
        });

        actualizarBotones();
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", configurarTema);
    } else {
        configurarTema();
    }
})();
