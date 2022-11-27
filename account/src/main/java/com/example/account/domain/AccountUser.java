package com.example.account.domain;

import lombok.*;

import javax.persistence.Entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
//@EntityListeners(AuditingEntityListener.class)
public class AccountUser extends BaseEntity{

    private String name;

}
