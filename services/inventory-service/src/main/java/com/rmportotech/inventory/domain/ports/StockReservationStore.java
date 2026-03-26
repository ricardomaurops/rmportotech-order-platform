package com.rmportotech.inventory.domain.ports;

import com.rmportotech.inventory.domain.model.StockReservation;

public interface StockReservationStore {
    void save(StockReservation stockReservation);
}