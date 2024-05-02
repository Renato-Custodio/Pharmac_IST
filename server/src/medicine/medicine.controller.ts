import { Body, Controller, Get, Post, Query } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { Medicine } from '@prisma/client';
import { CreateMedicineDto } from './dtos/CreateMedicine.dto';

@Controller('medicine')
export class MedicineController {
  constructor(private readonly medicineService: MedicineService) {}

  @Get('getAll')
  async findAll(@Query('pageNumber') pageNumber: number): Promise<Medicine[]> {
    return await this.medicineService.getMedicines(pageNumber);
  }

  @Get('search')
  async findQuery(
    @Query('query') query: string,
    @Query('pageNumber') pageNumber: number,
  ): Promise<Medicine[]> {
    return await this.medicineService.getQuery(query, pageNumber);
  }

  @Post()
  async createMedicine(
    @Body() createMedicineDto: CreateMedicineDto,
  ): Promise<Medicine> {
    return this.medicineService.createMedicine(createMedicineDto);
  }
}
