package net.sf.thingamablog;



import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;


/**
 * Wraps an input stream that blocks indefinitely to simulate timeouts on read(), skip(), and close(). The resulting
 * input stream is buffered and supports retrying operations that failed due to an InterruptedIOException. Supports
 * resuming partially completed operations after an InterruptedIOException REGARDLESS of whether the underlying stream
 * does unless the underlying stream itself generates InterruptedIOExceptions in which case it must also support
 * resuming. Check the bytesTransferred field to determine how much of the operation completed; conversely, at what
 * point to resume.
 */

public class TimeoutInputStream extends FilterInputStream
{

    private final long readTimeout;
    private final long closeTimeout;
    private boolean closeRequested = false;
    private Thread thread;
    private byte[] iobuffer;
    private int head = 0;
    private int length = 0;
    private IOException ioe = null;
    private boolean waitingForClose = false;
    private boolean growWhenFull = false;

    /**
     * Creates a timeout wrapper for an input stream.
     * @param in the underlying input stream
     * @param bufferSize the buffer size in bytes; should be large enough to mitigate Thread synchronization and context
     * switching overhead
     * @param readTimeout the number of milliseconds to block for a read() or skip() before throwing an
     * InterruptedIOException;  blocks indefinitely
     * @param closeTimeout the number of milliseconds to block for a close() before throwing an InterruptedIOException;
     *  blocks indefinitely, -1 closes the stream in the background
     */

    public TimeoutInputStream(InputStream in, int bufferSize, long readTimeout, long closeTimeout)
    {
        super(in);
        this.readTimeout = readTimeout;
        this.closeTimeout = closeTimeout;
        this.iobuffer = new byte[bufferSize];

        thread = new Thread(new Runnable()
        {
            public void run()
            {
                runThread();
            }

        }, "TimeoutInputStream");

        thread.setDaemon(true);
        thread.start();
    }

    public TimeoutInputStream(InputStream in, int bufferSize, long readTimeout, long closeTimeout, boolean growWhenFull)
    {
        this(in, bufferSize, readTimeout, closeTimeout);
        this.growWhenFull = growWhenFull;
    }

    /**
     * Wraps the underlying stream's method. 
     * It may be important to wait for a stream to actually be closed because
     * it holds an implicit lock on a system resoure (such as a file) while it is  open. 
     * Closing a stream may take time if the underlying stream is still servicing a previous request. 
     * @throws InterruptedIOException if the timeout expired 
     * @throws IOException if an i/o error occurs 
     */

    public void close() throws IOException
    {
        Thread oldThread;

        synchronized(this)
        {
            if(thread == null)
                return;

            oldThread = thread;
            closeRequested = true;
            thread.interrupt();
            checkError();
        }

        if(closeTimeout == -1)
            return;

        try
        {
            oldThread.join(closeTimeout);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        synchronized(this)
        {
            checkError();

            if(thread != null)
                throw new InterruptedIOException();
        }
    }

    /**
     * Returns the number of unread bytes in the buffer. 
     * @throws IOException if an i/o error occurs 
     */

    public synchronized int available() throws IOException
    {
        if(length == 0)
            checkError();

        return length > 0 ? length : 0;
    }

    /**
     * Reads a byte from the stream. 
     * @throws InterruptedIOException if the timeout expired and no data was received, 
     * bytesTransferred will be zero
     * 
     * @throws IOException if an i/o error occurs 
     */

    public synchronized int read() throws IOException
    {
        if(!syncFill())
            return -1;

        int b = iobuffer[head++] & 255;

        if(head == iobuffer.length)
            head = 0;

        length--;
        notify();
        return b;
    }

    /**
     * Reads multiple bytes from the stream. 
     * @throws InterruptedIOException if the timeout expired and no data was received, 
     * bytesTransferred will be zero
     * @throws IOException if an i/o error occurs 
     */

    public synchronized int read(byte[] buffer, int off, int len) throws IOException
    {
        if(!syncFill())
            return -1;

        int pos = off;
        if(len > length)
            len = length;

        while(len-- > 0)
        {
            buffer[pos++] = iobuffer[head++];
            if(head == iobuffer.length)
                head = 0;
            length--;
        }

        notify();
        return pos - off;
    }

    /**
     * Skips multiple bytes in the stream. 
     * @throws InterruptedIOException if the timeout expired before all of the 
     * bytes specified have been skipped,
     * bytesTransferred may be non-zero 
     * @throws IOException if an i/o error occurs 
     */

    public synchronized long skip(long count) throws IOException
    {
        long amount = 0;
        try
        {
            do
            {
                if(!syncFill())
                    break;

                int skip = (int)Math.min(count - amount, length);
                head = (head + skip) % iobuffer.length;
                length -= skip;
                amount += skip;
            }
            while(amount < count);
        }
        catch (InterruptedIOException e)
        {
            e.bytesTransferred = (int)amount;
            throw e;
        }

        notify();
        return amount;
    }

    /**
     * Mark is not supported by the wrapper even if the underlying stream does, returns false. 
     */

    public boolean markSupported()
    {
        return false;
    }

    /**
     *  Waits for the buffer to fill if it is empty and the stream has not reached EOF. 
     * @return true if bytes are available, false if EOF has been reached 
     * @throws InterruptedIOException if EOF not reached but no bytes are available 
     */

    private boolean syncFill() throws IOException
    {
        if(length != 0)
            return true;

        checkError();

        if(waitingForClose)
            return false;

        notify();

        try
        {
            wait(readTimeout);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        if(length != 0)
            return true;

        checkError();

        if(waitingForClose)
            return false;

        throw new InterruptedIOException();
    }

    /**
     * If an exception is pending, throw it. 
     */
    private void checkError() throws IOException
    {
        if(ioe != null)
        {
            IOException e = ioe;
            ioe = null;
            throw e;
        }
    }

    /**
     * Runs the thread in the background. 
     */

    private void runThread()
    {
        try
        {
            readUntilDone();
        }
        catch (IOException e)
        {
            synchronized(this)
            {
                ioe = e;
            }
        }
        finally
        {
            waitUntilClosed();
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                synchronized(this)
                {
                    ioe = e;
                }
            }
            finally
            {
                synchronized(this)
                {
                    thread = null;
                    notify();
                }
            }
        }
    }

    /**
     * Waits until we have been requested to close the stream. 
     */
    private synchronized void waitUntilClosed()
    {
        waitingForClose = true;
        notify();

        while(!closeRequested)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                closeRequested = true;
            }
        }
    }

    /**
     * Reads bytes into the buffer until EOF, closed, or error. 
     */

    private void readUntilDone() throws IOException
    {
        for(;;)
        {
            int off;
            int len;

            synchronized(this)
            {
                while(isBufferFull())
                {
                    if(closeRequested)
                        return;

                    waitForRead();
                }

                off = (head + length) % iobuffer.length;
                len = ((head > off) ? head : iobuffer.length) - off;
            }

            int count;
            try
            {
                count = in.read(iobuffer, off, len);
                if(count == -1)
                    return;
            }
            catch(InterruptedIOException e)
            {
                count = e.bytesTransferred;
            }

            synchronized(this)
            {
                length += count;
                notify();
            }
        }
    }

    private synchronized void waitForRead()
    {
        try
        {
            if(growWhenFull)
            {
                wait(readTimeout);
            }
            else
            {
                wait();
            }
        }
        catch (InterruptedException e)
        {
            closeRequested = true;
        }

        if(growWhenFull && isBufferFull())
        {
            growBuffer();
        }
    }

    private synchronized void growBuffer()
    {
        int newSize = 2 * iobuffer.length;
        if(newSize > iobuffer.length)
        {
            if(true)
            {
                System.out.println("InputStream growing to " + newSize + " bytes");
            }

            byte[] newBuffer = new byte[newSize];

            int pos = 0;
            int len = length;

            while(len-- > 0)
            {
                newBuffer[pos++] = iobuffer[head++];
                if(head == iobuffer.length)
                    head = 0;
            }

            iobuffer = newBuffer;
            head = 0;
        }
    }

    private boolean isBufferFull()
    {
        return length == iobuffer.length;
    }

}
