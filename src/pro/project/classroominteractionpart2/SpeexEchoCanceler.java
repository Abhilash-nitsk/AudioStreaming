package pro.project.classroominteractionpart2;

public class SpeexEchoCanceler
{

    public native static void open(int sampleRate, int bufSize, int totalLength);
    public native static short[] process(short[] inputframe, short[] echoframe);
    public native static void close();


    public synchronized static void openEcho(int sampleRate, int bufSize, int totalLength)
    {
        open(sampleRate,bufSize,totalLength);
    }

    public synchronized static short[] processEcho(short[] inputframe, short[] echoframe)
    {
        return process(inputframe, echoframe);
    }

    public synchronized static void closeEcho()
    {
        close();
    }

    static {
        System.loadLibrary("speex");
    }
}