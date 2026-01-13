package biometric.entel.util;

/**
 * Estimado programador:
 * Cuando escribí este código, solo Dios y yo sabíamos cómo funcionaba.
 * Ahora, solo Dios lo sabe!
 * Por lo tanto, si está intentando optimizar esta rutina y
 * falla (seguramente), aumente este contador como advertencia para la siguiente persona:
 * <p>
 * total_horas_wasted_here: 568
 * <p>
 * Creado por Epion el 9/10/2019
 */
public class JSGDUtils {

    private static final int wsqWidth = 512;
    private static final int wsqHeigth = 512;
    private static final byte fillColor = (byte) 255;

    private byte[] convertRawImageToWSQ(int width, int heigth, int widhtL, int widhtR, int heigthU, int heigthD, byte[] rawImage, int modW, int modH) {
        byte[] resizedFPImage = new byte[262144];
        int posRowFPImage = 0;
        int posRawImage = 0;

        try {
            if (heigth > 512) {

                for (int i = 0; i < resizedFPImage.length; i++) {

                    if (posRowFPImage > (widhtL) && posRowFPImage < (width + widhtR + (modW == 0 ? 1 : 0))) {
                        resizedFPImage[i] = rawImage[posRawImage];
                        posRawImage++;
                    } else {
                        resizedFPImage[i] = fillColor;
                    }

                    if (posRowFPImage == 511)
                        posRowFPImage = 0;
                    else
                        posRowFPImage++;
                }
            } else {
                for (int i = 0; i < resizedFPImage.length; i++) {
                    if (i >= (wsqHeigth * heigthU) && i <= (wsqHeigth * (heigth + heigthD - (modH == 0 ? 0 : 1)))) {
                        if (posRowFPImage > (widhtL) && posRowFPImage < (width + widhtR + (modW == 0 ? 1 : 0))) {
                            resizedFPImage[i] = rawImage[posRawImage];
                            posRawImage++;
                        } else {
                            resizedFPImage[i] = fillColor;
                        }

                        if (posRowFPImage == 511)
                            posRowFPImage = 0;
                        else
                            posRowFPImage++;
                    } else {
                        resizedFPImage[i] = fillColor;
                    }
                }
            }

            return resizedFPImage;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } catch (Throwable ex) {
            return null;
        }
    }

    /**
     * @param rawImage
     * @param widthVeridium
     * @param heightVeridium
     * @return
     */
    public byte[] getWSQfromRawImage(byte[] rawImage, int widthVeridium, int heightVeridium) {

        int diffW = wsqWidth - widthVeridium;
        int diffH = wsqHeigth - heightVeridium;

        int widhtL = (diffW / 2);
        int widhtR = (diffW / 2) + (diffW % 2);

        int heigthU = (diffH / 2);
        int heigthD = (diffH / 2) + (diffH % 2);

        return convertRawImageToWSQ(widthVeridium, heightVeridium, widhtL, widhtR, heigthU, heigthD, rawImage, diffW % 2, diffH % 2);
    }
}
