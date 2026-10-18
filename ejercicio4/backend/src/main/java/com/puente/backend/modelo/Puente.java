package com.puente.backend.modelo;

public class Puente {
    private String direccion = "NINGUNA";
    private int vehiculosNorte = 0;
    private int vehiculosSur = 0;
    private int conteoConsecutivoNorte = 0;
    private int conteoConsecutivoSur = 0;

    public Puente() {}

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public int getVehiculosNorte() { return vehiculosNorte; }
    public void setVehiculosNorte(int vehiculosNorte) { this.vehiculosNorte = vehiculosNorte; }

    public int getVehiculosSur() { return vehiculosSur; }
    public void setVehiculosSur(int vehiculosSur) { this.vehiculosSur = vehiculosSur; }

    public int getConteoConsecutivoNorte() { return conteoConsecutivoNorte; }
    public void setConteoConsecutivoNorte(int conteoConsecutivoNorte) { this.conteoConsecutivoNorte = conteoConsecutivoNorte; }

    public int getConteoConsecutivoSur() { return conteoConsecutivoSur; }
    public void setConteoConsecutivoSur(int conteoConsecutivoSur) { this.conteoConsecutivoSur = conteoConsecutivoSur; }
}
