import { IsNotEmpty, IsPositive, IsString } from 'class-validator';

export class CreateMedicineDto {
  @IsString()
  @IsNotEmpty()
  name: string;

  @IsString()
  @IsNotEmpty()
  purpose: string;

  @IsPositive()
  @IsNotEmpty()
  quantity: number;
}
