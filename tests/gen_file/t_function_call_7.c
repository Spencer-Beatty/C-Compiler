void one(){
    print_i(1);
}

int two(int i){
    if(i == 2){
        return 1;

    }else{
        one();
        return 2;
    }
}

void main(){
    print_i(two(2));

}