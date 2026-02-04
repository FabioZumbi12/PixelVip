package br.net.fabiozumbi12.pixelvip.compat;

public interface TaskHandle {
    void cancel();

    boolean isCancelled();
}
