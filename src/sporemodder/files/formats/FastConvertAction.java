package sporemodder.files.formats;

import java.io.File;

import sporemodder.files.MemoryOutputStream;
import sporemodder.files.formats.dbpf.DBPFPackingTask;

public interface FastConvertAction extends ConvertAction {

	public MemoryOutputStream fastConvert(File input, DBPFPackingTask outputDBPF) throws Exception;
}
