/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.utilities;

/**
 *
 * @author Daniel
 */
public enum Status {
    OK(0), NOT_IN_STOCK(1), DOES_NOT_EXIST(2);
    
    private final int value;
    private Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
