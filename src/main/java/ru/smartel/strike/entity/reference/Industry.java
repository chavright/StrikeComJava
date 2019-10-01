package ru.smartel.strike.entity.reference;

import ru.smartel.strike.entity.interfaces.Reference;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "industries")
public class Industry extends EntityWithNames implements Reference {

    public Industry(String nameRu, String nameEn, String nameEs) {
        super(nameRu, nameEn, nameEs);
    }

    public Industry() {
    }
}