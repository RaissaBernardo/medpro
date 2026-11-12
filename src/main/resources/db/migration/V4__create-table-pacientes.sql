create table pacientes (
    id bigint not null auto_increment,
    nome varchar(100) not null,
    email varchar(100) not null unique,
    cpf varchar(11) not null unique,
    telefone varchar(20),
    rua varchar(100),
    bairro varchar(100),
    cep varchar(9),
    cidade varchar(100),
    estado varchar(2),
    numero varchar(20),
    complemento varchar(100),
    primary key (id)
);
