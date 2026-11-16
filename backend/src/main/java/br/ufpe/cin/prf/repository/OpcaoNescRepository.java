package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.entity.OpcaoNesc;
import br.ufpe.cin.prf.entity.SubtipoNesc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcaoNescRepository extends JpaRepository<OpcaoNesc, Integer> {

    List<OpcaoNesc> findAllByOrderByCategoriaAscSubtipoAscTextoAsc();

    List<OpcaoNesc> findByCategoriaOrderBySubtipoAscTextoAsc(CategoriaNesc categoria);

    /** UNIQUE (categoria, subtipo, texto). */
    boolean existsByCategoriaAndSubtipoAndTexto(CategoriaNesc categoria, SubtipoNesc subtipo, String texto);

    boolean existsByCategoriaAndSubtipoAndTextoAndIdNot(CategoriaNesc categoria, SubtipoNesc subtipo,
                                                        String texto, Integer id);
}
