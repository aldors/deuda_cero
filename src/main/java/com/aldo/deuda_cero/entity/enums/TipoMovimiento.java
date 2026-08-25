package com.aldo.deuda_cero.entity.enums;

public enum TipoMovimiento {

    GASTO,
    PAGO // PODEMOS QUITARLO SIN PROBLEMA, gracias a PagoDeuda, el PAGO se puede representar como un pago realmente no como "compensasion" a base de otro movimiento
}
