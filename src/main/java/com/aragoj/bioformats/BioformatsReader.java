package com.aragoj.bioformats;

import com.aragoj.plugins.imagereader.ImageReaderPlugin;
import com.aragoj.plugins.imagereader.SupportedFormat;
import com.aragoj.plugins.imagereader.image.Image;
import com.aragoj.plugins.imagereader.image.PixelData;
import com.aragoj.plugins.imagereader.metadata.Metadata;
import com.aragoj.plugins.imagereader.metadata.MetadataItem;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.in.DefaultMetadataOptions;

public class BioformatsReader extends ImageReaderPlugin {

  private ImageReader reader;

  public BioformatsReader() {
    reader = new ImageReader();
  }

  @Override public String getPluginName() {
    return "Bioformats";
  }

  @Override public Image<? extends Buffer> readImage(String path)
      throws ImageReaderPlugin.FormatNotSupported {
    try {
      setupReader(path);
      Metadata metadata = retrieveMetadata();
      PixelData<ByteBuffer> pixelData = retrievePixelData();
      reader.close();
      return new Image<>(pixelData, metadata);
    } catch (FormatException | IOException e) {
      throw new ImageReaderPlugin.FormatNotSupported("File could not be parsed", e);
    }
  }

  private void setupReader(String path) throws IOException, FormatException {
    reader.close();
    reader.setOriginalMetadataPopulated(true);
    reader.setMetadataStore(MetadataTools.createOMEXMLMetadata());
    reader.setMetadataOptions(new DefaultMetadataOptions());
    reader.setId(path);
  }

  private Metadata retrieveMetadata() {
    return getMetadata(reader.getGlobalMetadata());
  }

  /**
   * Retrieves the pixel data. Only supports retrieving the first series if more than 1 exists.
   */
  private PixelData<ByteBuffer> retrievePixelData() throws IOException, FormatException {
    reader.setSeries(0);
    int width = reader.getSizeX();
    int height = reader.getSizeY();
    int pixelType = reader.getPixelType();

    byte[] rawData = reader.openBytes(0, 0, 0, width, height);

    byte[] processed = BioformatsPixelParser.parseRawData(rawData, width, height,
        FormatTools.getBytesPerPixel(pixelType), FormatTools.isFloatingPoint(pixelType),
        reader.isInterleaved(), FormatTools.isSigned(pixelType), reader.isLittleEndian(), null,
        null);

    return PixelData.createByteRgb(ByteBuffer.wrap(processed), width, height);
  }

  private Metadata getMetadata(Hashtable<String, Object> inputMetadata) {
    ArrayList<MetadataItem> items = new ArrayList<>();
    items.add(new MetadataItem("Width", "" + reader.getSizeX()));
    items.add(new MetadataItem("Height", "" + reader.getSizeY()));
    for (Map.Entry<String, Object> entry : inputMetadata.entrySet()) {
      items.add(new MetadataItem(entry.getKey(), entry.getValue()
          .toString()));
    }

    return new Metadata(items);
  }

  @Override public SupportedFormat[] getSupportedFormats() {
    return new SupportedFormat[] { SupportedFormat.ALL_FILES };
  }
}
