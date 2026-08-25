package com.aldo.deuda_cero.baseEntity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/*
Esta clase sirve para poner atributos que se repiten mucho entre las entidades, en este caso
en Usuario, Movimiento y Grupo se usan estos dos parametros.

En su lugar se crea esta clase con atributos base para entidades.

Cuando se desee implementar a esas 3 entidades, ademas se debe sustituir la anotacion @Builder por
@SuperBuilder. Esto es funciona bien cuando una entidad hereda de una BaseEntity.

ESTO DEJAR PARA DESPUES AGREGARLO EN OTRO MOMENTO
*/

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    protected LocalDateTime fechaCreacion;

    @LastModifiedDate
    protected LocalDateTime fechaActualizacion;

}
