create table despesas (
    id bigserial primary key,
    descricao varchar(160) not null,
    valor numeric(15, 2) not null,
    categoria varchar(30) not null,
    data_despesa date not null,
    forma_pagamento varchar(60),
    observacao varchar(500),
    origem_lancamento varchar(20) not null,
    texto_original varchar(2000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_despesas_data on despesas (data_despesa);
create index idx_despesas_categoria on despesas (categoria);

create table interacoes_ia (
    id bigserial primary key,
    conversation_id varchar(100) not null,
    origem varchar(20) not null,
    texto_original varchar(4000) not null,
    transcricao varchar(4000),
    resposta varchar(8000) not null,
    created_at timestamp with time zone not null
);

create index idx_interacoes_conversa_data on interacoes_ia (conversation_id, created_at);
