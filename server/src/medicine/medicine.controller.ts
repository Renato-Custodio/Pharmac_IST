import { Body, Controller, Get, Post, Query } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { Medicine } from '@prisma/client';
import { CreateMedicineDto } from './dtos/create-Medicine.dto';

@Controller('api/medicine')
export class MedicineController {
  constructor(private readonly medicineService: MedicineService) {}

  @Get('search')
  async findQuery(
    @Query('query') query: string,
    @Query('pageNumber') pageNumber?: number,
  ) {
    return await this.medicineService.getMedicines(query, pageNumber);
  }

  @Post()
  async createMedicine(
    @Body() createMedicineDto: CreateMedicineDto,
  ): Promise<Medicine> {
    return this.medicineService.createMedicine(createMedicineDto);
  }
}
