import java.util.ArrayList;


public class Wave {
    protected short bps;
    protected short type;
    protected short block_align;
    protected short num_channels;
    protected int sample_rate;
    protected int file_size;
    protected int data_size;
    protected int byte_rate;
    protected ArrayList <Short> samples_r;
    protected ArrayList <Short> samples_l;
    
    public Wave(){
        
    }
    
    public Wave(short bps, short type, short block_align, short num_channels, 
                int sample_rate, int file_size, int data_size, int byte_rate){
        this.bps = bps;
        this.type = type;
        this.block_align = block_align;
        this.num_channels = num_channels;
        this.sample_rate = sample_rate;
        this.file_size = file_size;
        this.data_size = data_size;
        this.byte_rate = byte_rate;
    }
    
    public Wave(short bps, short type, short block_align, short num_channels, 
                int sample_rate, int file_size, int data_size, int byte_rate,
                ArrayList<Short> samples_r, ArrayList<Short> samples_l){
        this.bps = bps;
        this.type = type;
        this.block_align = block_align;
        this.num_channels = num_channels;
        this.sample_rate = sample_rate;
        this.file_size = file_size;
        this.data_size = data_size;
        this.byte_rate = byte_rate;
        this.samples_r= new ArrayList(samples_r);
        this.samples_l= new ArrayList(samples_l);
    }
}
