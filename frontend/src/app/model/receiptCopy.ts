import { Expenditure } from "./expenditure";

export class ReceiptCopy {
    declare public id:number;

    date!: Date;
    photoPath!: string;
    translation!: string;
    expenditure!: Expenditure;

    public toString(): string {
        return this.id + ', ' + this.photoPath.toString();
    }    
}