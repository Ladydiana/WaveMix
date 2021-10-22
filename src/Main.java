/**
 * 
 * @author Culincu Diana Cristina
 */

import java.util.*;
import java.io.*;
import java.lang.*;
import java.nio.*;


public class Main {
    
    static Wave file1;
    static Wave file2;
    static Wave file_out;
    
    public static void readFile(File f, int file_number){
        int file_size;
        short audio_type, num_channels, block_align, bps;
        int sample_rate, byte_rate, data_size, i, j, delta;
        byte buff[]= new byte[4];
        byte buff2[]= new byte[2];
        byte buff1[] = new byte[1];
        byte buff_delta[];
        ByteBuffer buffer;
        ArrayList<Short> samples_r, samples_l;
        
        try{
            FileInputStream fis= new FileInputStream(f);
            
            //RIFF
            fis.read(buff);
            if(!(new String(buff)).equals("RIFF")){
                System.err.println("Error wrong RIFF format on file "+ f.getName());
                System.exit(1);
            }
            System.out.println("RIFF:\t"+new String(buff));
            
            //File size
            fis.read(buff);
            buffer = ByteBuffer.wrap(buff);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            file_size = buffer.getInt();
            System.out.println("File_size:\t"+file_size);
            
            //WAVE
            fis.read(buff);
            if(!(new String(buff)).equals("WAVE")){
                System.err.println("Error wrong WAVE format on file "+ f.getName());
                System.exit(1);
            }
            System.out.println("WAVE:\t"+new String(buff));
            
            //FMT
            fis.read(buff);
            if(!(new String(buff)).equals("fmt ")){
                System.err.println("Error wrong fmt format on file "+ f.getName());
                System.exit(1);
            }
            System.out.println("FMT:\t"+new String(buff));
            
            //Subchunk size
            fis.read(buff);
            System.out.println("Sub:\t"+((buff[0]<<24)&0xff000000|(buff[1]<<16)&0xff0000|(buff[2]<<8)&0xff00|(buff[3]<<0)&0xff));
            
            //Audio type format
            fis.read(buff2);
            buffer = ByteBuffer.wrap(buff2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  
            audio_type = buffer.getShort();
            System.out.print("Audio type:\t"+audio_type);
            if(audio_type == 1)
                System.out.print("\tPCM\n");
            else if(audio_type == 3)
                System.out.print("\tIEEE float\n");
            else if(audio_type == 6)
                System.out.print("\tA-law\n");
            else if(audio_type == 3)
                System.out.print("\tu-law\n");
            else if(audio_type == 0xffff)
                System.out.print("\tdetermined by subformat\n");
            else{
                 System.err.print("\tWrong audio type format\n");
                 System.exit(1);
            }
            
            //Num channels
            fis.read(buff2);
            buffer = ByteBuffer.wrap(buff2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            num_channels = buffer.getShort();
            if(num_channels!=1 && num_channels!=2){
                System.err.print("\tWrong num_channels format\n");
                System.exit(1);
            }
            else
                System.out.println("No channels:\t"+num_channels);
            
            //Sample rate
            fis.read(buff);
            buffer = ByteBuffer.wrap(buff);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            sample_rate = buffer.getInt();
            System.out.println("Sample rate:\t"+sample_rate);
            
            //Byte rate
            fis.read(buff);
            buffer = ByteBuffer.wrap(buff);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            byte_rate = buffer.getInt();
            System.out.println("Byte rate:\t"+byte_rate);
            
            //Block align
            fis.read(buff2);
            buffer = ByteBuffer.wrap(buff2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            block_align = buffer.getShort();
            System.out.println("Block align:\t"+block_align);
            
            //BPS
            fis.read(buff2);
            buffer = ByteBuffer.wrap(buff2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            bps = buffer.getShort();
            System.out.println("BPS:\t"+bps);
            
            //data
            fis.read(buff1);
            while(true){
                if((new String(buff1)).equals("d")){
                    fis.read(buff1);
                    if((new String(buff1)).equals("a")){
                        fis.read(buff1);
                        if((new String(buff1)).equals("t")){
                            fis.read(buff1);
                            if((new String(buff1)).equals("a"))
                                break;
                        }
                    }
                }
            }
            //System.out.println("Done with data");
            
            //Data size
            fis.read(buff);
            buffer = ByteBuffer.wrap(buff);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            data_size = buffer.getInt();
            System.out.println("Data size:\t"+data_size);
            
            //More initis
            samples_r = new ArrayList <>(data_size/num_channels);
            if(num_channels == 2)
                samples_l = new ArrayList<>(data_size/2);
            else samples_l = new ArrayList<>();
            delta=bps/8;
            j=0;
            buff_delta= new byte[delta];
            
            
            //Read data into sample arrays
            for(i=0; i<data_size; i+=delta){
                fis.read(buff_delta);
                buffer = ByteBuffer.wrap(buff_delta);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                samples_r.add(buffer.getShort());
                if(num_channels == 2){
                    fis.read(buff_delta);
                    buffer = ByteBuffer.wrap(buff_delta);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    samples_l.add(buffer.getShort());
                    i+= delta;
                }
            }
            fis.close();
            
            System.out.println("Done reading samples");
            
            if(file_number == 1)
                file1 = new Wave(bps, audio_type, block_align, num_channels, sample_rate,
                            file_size, data_size, byte_rate, samples_r, samples_l);
            if(file_number == 2)
                file2 = new Wave(bps, audio_type, block_align, num_channels, sample_rate,
                            file_size, data_size, byte_rate, samples_r, samples_l);
        }
        catch(IOException e){
            System.out.println("Eroare la citire\n"+ e.getMessage());
            System.exit(1);
        }
    }
    
    
    public static void writeFile(Wave w){
        
        if(w == null){
            System.err.println("Bad write file format");
            System.exit(1);
        }
        try{
            FileOutputStream fos= new FileOutputStream(new File("out.wav"));
            int limit;
            
            //Putting the header
            fos.write(new String("RIFF").getBytes());
            fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(w.file_size).array());
            fos.write(new String("WAVE").getBytes());
            fos.write(new String("fmt ").getBytes());
            fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array());
            fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.type).array());
            fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.num_channels).array());
            fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(w.sample_rate).array());
            fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(w.byte_rate).array());
            fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.block_align).array());
            fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.bps).array());
            fos.write(new String("data").getBytes());
            fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(w.data_size).array());
            
            limit = w.bps / 8;
            limit = w.data_size / (limit * w.num_channels);
            
            if(w.num_channels == 1)
                for(int i=0; i<limit; ++i)
                    fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.samples_r.get(i)).array());
            else if(w.num_channels == 2)
                for(int i=0; i<limit; ++i){
                    fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.samples_r.get(i)).array());
                    fos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(w.samples_l.get(i)).array());
                }
            System.out.println("Done writing output file");
            fos.close();
        }
         catch(IOException e){
            System.err.println("Eroare la scriere\n"+ e.getMessage());
            System.exit(1);
        }
    }
    
    public static int CMMDC(int a, int b){
        int c;
        while (a != 0){
            c = a;
            a = b % a;
            b = c;
        }
        return b;
    }
    
    
    public static void upsampling(Wave w, int u){
        if ((u < 2) || (w == null)){
            System.err.println("Eroare la upsampling. null w");
            System.exit(1);
        }
        
        short[] dest_r;
        short[] dest_l;
        int max, i, j, index = 0;

        /* resample right */
        dest_r = new short [(w.data_size * u) / w.num_channels];
        max = (w.data_size * 8) / (w.bps * w.num_channels);
  
        for (i = 0; i < max; ++i)
            for (j = 0; j < u; ++j){
                dest_r[index] = w.samples_r.get(i);
                index++;
            }
        
        w.sample_rate = w.sample_rate * u;
        w.file_size = w.file_size + ((u-1) * w.data_size);
        w.data_size = u * w.data_size;
        w.samples_r = new ArrayList(Arrays.asList(dest_r));

        /* resample left */
        if (w.num_channels == 2){    
            dest_l = new short [(w.data_size * u) / 2];
            index = 0;
            for (i = 0; i < max; ++i)
                  for (j = 0; j < u; ++j){
                      dest_l[index] = w.samples_l.get(i);
                      ++index;
                  }
            w.samples_r = new ArrayList(Arrays.asList(dest_l));
        }
        
    }
    
    
    public static void downsampling(Wave w, int d){
        if ((d < 2) || (w == null)){
            System.err.println("Eroare la upsampling. null w");
            System.exit(1);
        }
        
        short[] dest_r;
        short[] dest_l;
        int max, i, index = 0;

        /* resample right - data_size should divide d */
        dest_r = new short [w.data_size / (d * w.num_channels)];
        max = (w.data_size * 8) / (w.bps * w.num_channels);

        index = 0;
        for (i = 0; i < max; i += d){
            dest_r[index] = w.samples_r.get(i);
            ++index;
        }

        w.sample_rate = w.sample_rate / d;
        w.file_size = w.file_size - ((d-1) * (w.data_size/d));
        w.data_size = w.data_size / d;
        w.samples_r = new ArrayList(Arrays.asList(dest_r));

        /* resample left */
        if (w.num_channels == 2){    
            dest_l = new short [w.data_size / (2 * d)];
            index = 0;
            for (i = 0; i < max; i += d){
                dest_l[index] = w.samples_l.get(i);
                ++index;
            }
            w.samples_l = new ArrayList(Arrays.asList(dest_l));
        }

    }
    
    
    public static void resampling(Wave w, int a, int b){
        if (w == null){
            System.err.println("Eroare la resampling. null w");
            System.exit(1);
        }
  
        upsampling (w, a);
        downsampling (w, b);
    }
    
    
    public static void mix(float t){
        
        //Checking if the files match
        if(file1 == null || file2 == null){
            System.err.println("Eroare la mix. null file");
            System.exit(1);
        }
        
        if ((t > 1) || ((t < 0) && (t != -1))){
            System.err.println("Eroare la mix. Bad t value");
            System.exit(1);
        }
  
        if (file1.num_channels != file2.num_channels){
        System.err.println("Mismatching channels for wav files");
        System.exit(1);
        }
        
        //Resample
        if (file1.sample_rate != file2.sample_rate){
            int up, down;
            if (file1.sample_rate > file2.sample_rate){
                if ((file1.sample_rate % file2.sample_rate) == 0){
                    up = file1.sample_rate / file2.sample_rate;
                    upsampling (file2,up);
                }
                else{
                    up = CMMDC (file1.sample_rate,file2.sample_rate);
                    down = file2.sample_rate / up;
                    up = file1.sample_rate / up;
                    resampling (file2,up,down);
                }
            }
            else{  /* file2 > file1 */
                if ((file2.sample_rate % file1.sample_rate) == 0){
                    up = file2.sample_rate / file1.sample_rate;
                    upsampling (file1,up);
                }
                else{
                      up = CMMDC (file1.sample_rate,file2.sample_rate);
                      down = file1.sample_rate / up;
                      up = file2.sample_rate / up;
                      resampling (file1,up,down);
                }
            }
        }
        
        /* composing the out file header data */
        file_out.bps = file1.bps;
        file_out.type = file1.type;
        file_out.block_align = file1.block_align;
        file_out.num_channels = file1.num_channels;
        file_out.sample_rate = file1.sample_rate;
        file_out.byte_rate = file1.byte_rate;
        
        
        /* max data size; mix will last until min(playing_time) */
        int min, max;
        if (file1.data_size < file2.data_size){
            min = (file1.data_size * 8) / (file1.num_channels * file1.bps);
            file_out.data_size = file2.data_size;
            file_out.file_size = file_out.data_size + 44;
        }
        else {
            min = (file2.data_size * 8) / (file2.num_channels * file2.bps);
            file_out.data_size = file1.data_size;
            file_out.file_size = file_out.data_size + 44;
        }
        
        //System.out.println(file_out.data_size);
        max = (file_out.data_size * 8) / (file_out.num_channels * file_out.bps);
        
        file_out.samples_r = new ArrayList<>((file_out.data_size/file_out.num_channels));
        for(int i=0; i<file_out.data_size/file_out.num_channels; ++i)
            file_out.samples_r.add(Short.MIN_VALUE);
       
        
        if (file_out.num_channels == 2){
            file_out.samples_l = new ArrayList<> (file_out.data_size/2);
            for(int i=0; i<file_out.data_size/2; ++i)
                file_out.samples_l.add(Short.MIN_VALUE);
        }
        int i, c=0;
        
        if (t == -1){
            int lim_1 = min/3, lim_2 = min/2;
            float delta = (float)5/min;
            for (i = 0; i < lim_1; ++i)
                file_out.samples_r.set(i, file1.samples_r.get(i));
            t = 1;
            for (i = lim_1; i < lim_2; ++i){
                file_out.samples_r.set(i, (short) ((file1.samples_r.get(i) * t) + ((1-t) * (file2.samples_r.get(i)))));
                t -=  delta;
                if (t < 0)
                    t += delta;
            }
            
            for (i = lim_2; i < min; ++i)
                file_out.samples_r.set(i, file2.samples_r.get(i));
            
            if (file_out.num_channels == 2){
                for (i = 0; i < lim_1; ++i)
                    file_out.samples_l.set(i, file1.samples_l.get(i));
                t = 1;
                for (i = lim_1; i < lim_2; ++i){
                    file_out.samples_l.set(i, (short) ((file1.samples_l.get(i) * t) + ((1-t) * (file2.samples_l.get(i)))));
                    t -= delta;
                    if (t < 0)
                        t += delta;
                }
                for (i = lim_2; i < min; ++i)
                    file_out.samples_l.set(i, file2.samples_l.get(i));
              }
            c= 1;
        }
        
        if(c==0) {
            for (i = 0; i < min; ++i)
                file_out.samples_r.set(i, (short) ((file1.samples_r.get(i) * t) + (1-t) * (file2.samples_r.get(i))));

            if (file_out.num_channels == 2)
                for (i = 0; i < min; ++i)
                    file_out.samples_l.set(i, (short) ((file1.samples_l.get(i) * t) + (1-t) * (file2.samples_l.get(i))));
        }
        
        
        if (min == max)
                return;

        /* until max */
        if (file1.data_size < file2.data_size){
            for (i = min; i < max; ++i)
                file_out.samples_r.set(i, file2.samples_r.get(i));

            if (file_out.num_channels == 2)
                for (i = min; i < max; ++i)
                    file_out.samples_l.set(i, file2.samples_l.get(i));
        }
        else{
            for (i = min; i < max; ++i)
                file_out.samples_r.set(i, file1.samples_r.get(i));

            if (file_out.num_channels == 2)
                for (i = min; i < max; ++i)
                    file_out.samples_l.set(i, file1.samples_l.get(i));
        } 
    }
    
    
    
    
    public static void main(String args[]){
        if(args.length<2){
            System.err.println("Wrong usage.");
            System.exit(1);
        }
        
        
        // Citim fisierele .wave
        readFile(new File(args[0]), 1);
        readFile(new File(args[1]), 2);
        
        
        float t;
        if (args.length >= 3)
            t = Float.parseFloat(args[2].toString());
        else
            t = (float)0.5;
        
        file_out= new Wave();
        
        //readFile(new File("explosion1.wav"),1);
       // readFile(new File("explosion2.wav"),2);
        mix(t);
        
        writeFile(file_out);
    }
}
