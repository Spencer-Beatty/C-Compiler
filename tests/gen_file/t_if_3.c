// testing nested if clause followed by nested else clause

void main(){
    if(1 == 1){
        print_i(1);
        if(1 > 0){
            print_i(2);
            if(3){
                print_i(3);
            }
        }else{
            print_i(-1);
        }

    }


}