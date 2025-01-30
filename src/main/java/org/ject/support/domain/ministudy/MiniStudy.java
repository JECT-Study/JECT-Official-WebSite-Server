package org.ject.support.domain.ministudy;

import jakarta.persistence.*;
import org.ject.support.common.entity.BaseTimeEntity;

@Entity
public class MiniStudy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 2083)
    private String linkUrl;

    @Column(length = 2083)
    private String imageUrl;
}
