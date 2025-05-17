package net.sinedkadis.terracompositio.api.networks.cfe;

public interface ICFEHandler {
    int getCFE();
    int takeCFE(int cfe);
    default int addCFE(int cfe){
        return -takeCFE(-cfe);
    }
    void setCFE(int cfe);
    int getMaxCFE();
    int getMinCFE();
}
