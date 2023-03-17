char a11; char a12; char a13;
char a21; char a22; char a23;
char a31; char a32; char a33;
char empty;



int set(char row, int col, char mark) {
  int r;
  r = 1;
  if (row == 'a') {
     if (col == 1) {
        if (a11 == empty)
	    a11 = mark;
	else
	  r = -1;
     } else {
       if (col == 2) {
         if (a12 == empty)
           a12 = mark;
	 else
	   r = -1;
       } else {
         if (col == 3) {
	   if (a13 == empty)
              a13 = mark;
	   else
	     r = -1;
         } else {
           r = 0;
         }
       }
    }
  } else {
    if (row == 'b') {
       if (col == 1) {
          if (a21 == empty)
            a21 = mark;
	  else
	    r = -1;
       } else {
         if (col == 2) {
	    if (a22 == empty)
              a22 = mark;
	    else
	      r = -1;
         } else {
	    if (col == 3) {
	      if (a23 == empty)
                 a23 = mark;
              else
	       r = -1;
            } else {
	      r = 0;
	    }
	}
      }
    } else {
     if (row == 'c') {
        if (col == 1) {
	   if (a31 == empty)
             a31 = mark;
	   else
	     r = -1;
        } else {
           if (col == 2) {
	      if (a32 == empty)
                a32 = mark;
              else
	        r = -1;
           } else {
              if (col == 3) {
	        if (a33 == empty)
                   a33 = mark;
		else
		  r = -1;
	      } else {
	        r = 0;
	      }
          }
        }
     } else {
       r = 0;
     }
   }
  }
 return r;
}

void main(){
    print_i(set('a',2,'x'));

}