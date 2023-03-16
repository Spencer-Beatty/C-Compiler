int i;
int j;

void main(){
    i = 0;
    while(i < 10){
        int j;
        j = 0;
        while(j < 10){
            j = j + 1;
            print_i(j);
        }
        print_s((char *) "\n");
        i = i + 1;
    }
}