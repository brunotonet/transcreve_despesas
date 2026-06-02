package br.com.tonegobbi.transcrevedespesas.repository;

import br.com.tonegobbi.transcrevedespesas.entity.Despesa;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findAllByOrderByDataDespesaDescIdDesc();

    List<Despesa> findByDataDespesaBetweenOrderByDataDespesaDescIdDesc(LocalDate inicio, LocalDate fim);

    List<Despesa> findByCategoriaOrderByDataDespesaDescIdDesc(CategoriaDespesa categoria);

    Optional<Despesa> findTopByOrderByValorDescIdDesc();

    @Query("select coalesce(sum(d.valor), 0) from Despesa d where d.dataDespesa between :inicio and :fim")
    BigDecimal totalPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select d.categoria as categoria, sum(d.valor) as total from Despesa d group by d.categoria order by d.categoria")
    List<TotalCategoriaProjection> totalPorCategoria();

    interface TotalCategoriaProjection {
        CategoriaDespesa getCategoria();
        BigDecimal getTotal();
    }
}
