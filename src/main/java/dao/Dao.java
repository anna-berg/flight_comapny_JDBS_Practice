package dao;

import dto.TicketFilter;
import entity.TicketEntity;

import java.util.List;
import java.util.Optional;

public interface Dao <K, E> {
//    List<TicketEntity> findAll(TicketFilter filter);
    List<E> findAll();
    Optional<E> findById(K id);
    void update (E ticket);
    E save(E ticket);
    boolean delete (K id);
}
