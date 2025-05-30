package net.sinedkadis.terracompositio.api.networks.cfe;

public interface ICFEHandler {
    int getCFE();
    int takeCFE(int cfe,boolean simulate);
    int addCFE(int cfe,boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();
    int getMinCFE();
}
