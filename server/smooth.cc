/*
	predoc_png2gif

	Reads a PNG file, scales it down by factor 4 and writes the resulting
	image into a GIF file
	
	the program needs:
	- zlib
	- libpng
	- giflib

*/

// #define DEBUG

// #define ENABLE_GIF

#include <stdexcept>
#include <iostream>
#include <string>

typedef unsigned char byte;
typedef unsigned long ulong;

extern "C" {
	#include <png.h>
#ifdef ENABLE_GIF
	#include <gif_lib.h>
#endif
}

class TPNG
{
	public:
		TPNG();
		~TPNG() { _Clear(); }
		png_uint_32 width;
		png_uint_32 height;
		int bit_depth;
		int color_type;
		int interlace_type;
		int compression_type;
		int filter_type;
		byte **row_pointers;

		void load(const string&);
		void save(const string&);
#ifdef ENABLE_GIF
		void saveGIF(const string&);
#endif
		void supersample();
		
	protected:
		void _Clear();
};

void SmoothPNGFile(const string &file)
{
	TPNG image;
	cout << "  loading file " << file << endl;
	image.load(file);
	cout << "  smooting file " << file << endl;
	image.supersample();
#ifndef DEBUG
	cout << "  saving file " << file << endl;
	image.save(file);
#endif
	cout << "  done with file " << file << endl;
}

#ifdef DEBUG
int main()
{
	SmoothPNGFile("data/main.ps.raster/1.png");
	return 0;
}
#endif

TPNG::TPNG()
{
	width = height = 0;
	interlace_type = PNG_INTERLACE_NONE;
	compression_type = PNG_COMPRESSION_TYPE_DEFAULT;
	filter_type = PNG_FILTER_TYPE_DEFAULT;
}

void TPNG::load(const string &file)
{
	cout << "reading " << file << endl;
	FILE *fp = fopen(file.c_str(), "rb");
	if (!fp) {
		perror("open");
		return;
	}
	
	// check header
	//-------------------------------------------------------
	unsigned char header[10];
	if (fread(header, 1, 4, fp)!=4) {
		perror("read");
		return;
	}
	int is_png = png_check_sig(header, 4);
	if (!is_png) {
		cerr << "not a PNG file" << endl;
		fclose(fp);
		return;
	}
	
	// create data structures
	//-------------------------------------------------------
	png_structp png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
	if (!png_ptr) {
		return;
	}

	png_set_sig_bytes(png_ptr, 4);
	
	png_infop info_ptr = png_create_info_struct(png_ptr);
	if (!info_ptr) {
		png_destroy_read_struct(&png_ptr, (png_infopp)NULL, (png_infopp)NULL);
		return;
	}
	
	png_infop end_info = png_create_info_struct(png_ptr);
	if (!end_info) {
		png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp)NULL);
		return;
	}
	
	if (setjmp(png_ptr->jmpbuf)) {
		png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		fclose(fp);
		return;
	}
	
	// init IO
	//-------------------------------------------------------
	png_init_io(png_ptr, fp);
	
	// read header
	png_read_info(png_ptr, info_ptr);

	// get data from header	
	png_get_IHDR(png_ptr, info_ptr, 
		&width, &height,
		&bit_depth, 
		&color_type, 
		&interlace_type,
		&compression_type, 
		&filter_type);
	
	#if 0
	// remove alpha channel
	if (color_type & PNG_COLOR_MASK_ALPHA)
		png_set_strip_alpha(png_ptr);
	#endif

	png_color_8 sb;
	sb.red = 8;
	sb.green = 8;
	sb.blue = 8;
	sb.gray = 8;
	sb.alpha = 8;
  png_set_shift(png_ptr, &sb);
                                     	
	// update info structure
	png_read_update_info(png_ptr, info_ptr);

	// read image
	row_pointers = new unsigned char*[height];
	for (unsigned i=0; i<height; i++) {
		row_pointers[i] = (unsigned char*)malloc(png_get_rowbytes(png_ptr, info_ptr));
	}
	png_read_image(png_ptr, row_pointers);

	png_read_end(png_ptr, NULL);

	png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);

	cout << "loaded PNG" << endl;

	fclose(fp);
}

void TPNG::save(const string &file)
{
	cout << "writing " << file << endl;
	FILE *fp = fopen(file.c_str(), "wb");
	if (!fp)
		throw runtime_error("couldn't open PNG file");
	
	png_structp png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
	if (!png_ptr)
		throw runtime_error("couldn't create PNG write structure");
		
	png_infop info_ptr = png_create_info_struct(png_ptr);
	if (!info_ptr) {
		png_destroy_write_struct(&png_ptr, (png_infopp)NULL);
		throw runtime_error("couldn't create PNG info structure");
	}
	
	if (setjmp(png_ptr->jmpbuf)) {
		png_destroy_write_struct(&png_ptr, &info_ptr);
		fclose(fp);
		throw runtime_error("couldn't write PNG info structure");
	}
	
	png_init_io(png_ptr, fp);
	
//	png_set_write_status_fn(png_ptr, write_row_callback);
	
	png_set_filter(png_ptr, 0, PNG_FILTER_NONE);
	
	png_set_compression_level(png_ptr, Z_BEST_COMPRESSION);
	png_set_compression_mem_level(png_ptr, 8);
	png_set_compression_strategy(png_ptr, Z_DEFAULT_STRATEGY);
	png_set_compression_window_bits(png_ptr, 15);
	png_set_compression_method(png_ptr, 8);
	
	png_set_IHDR(png_ptr, info_ptr,
		width,
		height,
		bit_depth,
		color_type,
		interlace_type,
		compression_type,
		filter_type
	);
	
	// even more data...
	
	png_write_info(png_ptr, info_ptr);
	
	png_write_image(png_ptr, row_pointers);
	
	png_write_end(png_ptr, info_ptr);
	
	png_destroy_write_struct(&png_ptr, &info_ptr);
	free(info_ptr);
	
	fclose(fp);
	
	cout << "wrote PNG" << endl;
}

#ifdef ENABLE_GIF

void TPNG::saveGIF(const string &file)
{
	cout << "writing " << file << endl;
#if 0
	FILE *fp = fopen(file.c_str(), "wb");
	if (!fp)
		throw runtime_error("couldn't open GIF file");

	EGifSpew(GifFileType *GifFile, int fp)
#endif
	ColorMapObject *cmap =  MakeMapObject(256, NULL);
	if (cmap==NULL)
		throw runtime_error("couldn't create GIF colormap");
	for(int i=0; i<256; i++) {
		cmap->Colors[i].Red   = i;
		cmap->Colors[i].Green = i;
		cmap->Colors[i].Blue  = i;
	}

	GifFileType *gt = EGifOpenFileName(file.c_str(), false);

	//	EGifSetGifVersion("87a");

	if (EGifPutScreenDesc(gt, width, height, 256, 0, cmap)==GIF_ERROR)
		throw runtime_error("couldn't put GIF screen description");	
	if (EGifPutImageDesc(gt, 0,0, width, height, false, NULL)==GIF_ERROR)
		throw runtime_error("couldn't put GIF image description");
		
	for(int y=0; y<height; y++) {
		if (EGifPutLine(gt, row_pointers[y], width)==GIF_ERROR)
			throw runtime_error("couldn't put GIF scan line");
	}
	
	if (EGifCloseFile(gt)==GIF_ERROR)
		throw runtime_error("couldn't close GIF file");
}
#endif

/*
	scales down the image by factor 4
*/
void TPNG::supersample()
{
	if (color_type!=PNG_COLOR_TYPE_GRAY) {
		cerr << "scaling is only supported for gray images, sorry dude..." << endl;
		return;
	}

	// size of new image
	unsigned x,y, dx,dy;
	unsigned dw = (width >>2);
	unsigned dh = (height>>2);
	
	// memory for new image
	byte **rp = new unsigned char*[dh];
	for (y=0; y<dh; y++) {
		rp[y] = (unsigned char*)malloc(dw);
		memset(rp[y],0,dw);
	}
	
	// scale down
	for(dy=0, y=0; y<height-3; dy++, y+=4) {
		for(dx=0, x=0; x<width-3; dx++, x+=4) {
			unsigned gray;
			gray = row_pointers[y  ][x]+row_pointers[y  ][x+1]+row_pointers[y  ][x+2]+row_pointers[y  ][x+3]
           + row_pointers[y+1][x]+row_pointers[y+1][x+1]+row_pointers[y+1][x+2]+row_pointers[y+1][x+3]
           + row_pointers[y+2][x]+row_pointers[y+2][x+1]+row_pointers[y+2][x+2]+row_pointers[y+2][x+3]
           + row_pointers[y+3][x]+row_pointers[y+3][x+1]+row_pointers[y+3][x+2]+row_pointers[y+3][x+3];
			rp[dy][dx] = gray / 16;
		}
	}

	_Clear();

	width = dw;
	height = dh;
	row_pointers = rp;
}

void TPNG::_Clear()
{
	if (row_pointers) {
		// remove old image
		for(int y=0; y<height; y++) {
			free(row_pointers[y]);
		}
		free(row_pointers);
		row_pointers = NULL;
	}
}
