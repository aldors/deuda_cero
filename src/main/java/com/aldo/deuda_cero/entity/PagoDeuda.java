package com.aldo.deuda_cero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagos_deuda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDeuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deuda_id", nullable = false)
    private Deuda deuda;

    /*
    //¿Por q quitarlo? -> porque se puede obtener grupo, pagador y receptor directamente de Deuda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;
    */
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id", nullable = false)
    private MiembroGrupo pagador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    private MiembroGrupo receptor;
    */
   
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    private LocalDateTime fechaCreacion;
}

/*
Ok, hagamoslo, pero antes resuelveme una duda que tengo,
habiamos dicho que podiamos registrar movimientos, como gastos en los cuales la persona que cree
el movimiento participe y otro tipo de gasto en el que la persona que lo cree no participe, siendo asi simulado
el "prestamo" y por ultimo se podrian generar movimientos de tipo pago para saldar deudas,
eso esta en el enum TipoMovimiento, entonces hasta ahora se sabe que los pagos se pueden hacer como Movimientos
entonces hasta ahi todo bien, se hace lo mismo si se quiere simular un prestamo, si se quiere registrar
un gasto o si se quiere hacer un pago/saldar deuda (que seria como un prestamo a la inversa pero ahora del deudor al acreedor),
aqui te dejo los 3 casos:

1. Gasto normal, pago yo Aldo (usuario 1) y y ademas participo junto con isaac (usuario 2)
{
    "descripcion": "Gasto",
    "montoTotal": 500.00,
    "pagadorId": 1,
    "tipoMovimiento": "GASTO",
    "tipoDivision": "PERSONALIZADA",
    "participantes": [
        {
            "usuarioId": 1,
            "monto": 100.00
        },
        {
            "usuarioId": 2,
            "monto": 400.00
        }
    ]

}

2. Gasto como prestamo, pago yo Aldo y no participo, solo participa isaac - yo le presto a isaac
{
    "descripcion": "Gasto",
    "montoTotal": 500.00,
    "pagadorId": 1,
    "tipoMovimiento": "GASTO",
    "tipoDivision": "PERSONALIZADA",
    "participantes": [
        {
            "usuarioId": 2,
            "monto": 500.00
        }
    ]

}

3. Pago, paga isaac y participo solo yo Aldo - isaac me hace un prestamo a mi, pero en realidad me esta pagando
{
    "descripcion": "Gasto",
    "montoTotal": 500.00,
    "pagadorId": 2,
    "tipoMovimiento": "PAGO",
    "tipoDivision": "PERSONALIZADA",
    "participantes": [
        {
            "usuarioId": 1,
            "monto": 500.00
        }
        }
    ]

}

Pero ahora que me contaste eso de como saber quien ya saldo sus deudas
me surgio la duda de ¿por que hacer un nuevo diseño para registrar pagos, si ya se tiene la 
opcion de registrar un pago como si fuera un movimiento?
Me puse a pensar y me di cuenta de que asi no se puede representar el "x miembro pago x cantidad a x miembro",
¿me entiendes?, entonces me puse a pensar nuevamente y ahora con la estructura que planteas me di
cuenta de que si se puede representar gracias a los atributos (receptorId y monto) del request
para registrar pagos, ¿aqui que me siguieres? ya que seria como dos formas para saldar/pagar deudas, una 
en forma de Movimiento de tipo PAGO como prestamo a la inversa de deudor a acredor y la otra pasando el 
receptorId y el monto, podriamos eliminar el PAGO como tipo de movimiento y dejarlo como me lo planteas ahora 
o dejarlo, porque de alguna manera si A presto a B, ahora B debe a A, pero luego B presto a A, entonces
ahi A no tiene deuda con B porque B en forma de prestamo saldo su deuda con A, ¿entiendes?, es un poco confuso y alomejor
estoy un poco perdido pero tengo esas dudas y creo que ahi ya se esta formando un mal diseño.
Una forma en como yo lo entenderia es que un PAGO como movimiento se hace, funciona y salda la deuda pero ahi no 
sabemos quien pago a quien ni cuanto, y con la otra forma que me planteas para los pagos si sabemos ya que estan los datos del
pagador, receptor y monto en la base de datos
*/
