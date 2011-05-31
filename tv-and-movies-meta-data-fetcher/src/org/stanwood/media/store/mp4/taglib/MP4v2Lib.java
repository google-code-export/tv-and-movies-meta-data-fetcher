package org.stanwood.media.store.mp4.taglib;




public class MP4v2Lib {

	public static void dumpFileDetailsToStdout(String fileName) {
		MP4v2Library lib = MP4v2Library.INSTANCE;
		int fileHandle = lib.MP4Read(fileName,0);
		try {
			lib.MP4Dump(fileHandle,null,false);
		}
		finally {
			lib.MP4Close(fileHandle);
		}
	}

    public static void main(String[] args) {
    	String fileName = "/mounts/home/jp/workspaces/Java/tv-and-movies-meta-data-fetcher/tv-and-movies-meta-data-fetcher/tests/org/stanwood/media/testdata/a_video.mp4";
//    	dumpFileDetailsToStdout(fileName);
    	MP4v2Library lib = MP4v2Library.INSTANCE;
    	int fileHandle = lib.MP4Read(fileName,0);
		try {
			MP4Tags tags = new MP4Tags();
			lib.MP4TagsFetch(tags, fileHandle);
		}
		finally {
			lib.MP4Close(fileHandle);
		}

//    	System.out.println(lib.MP4FileInfo(fileName, 0));
//    	  lib.MP4ItmfGetItems(fileHandle);

    }
}
